# 009 绑定层形状、真机常量与 mock 对齐（第二轮真机风险自查）

状态：**已修复**（绑定库 0.1.0-SNAPSHOT，2026-09-29；
离线回归 8 组 × 两种 id 模式全过，外部 IntelliJ 工程产物同样 8/8）

关联：`008` 修的是"JS 值当 Java 类型硬用"；这一轮修的是"绑定层对 JS 形状的下注"——
包括 `/game/visual` 是类不是函数、mock 常量抄错、mock 行为缺口。

## 背景：怎么找出来的

`008` 修完后，把"绑定层假设的 JS 形状"和"游戏真正的形状"系统对了一遍，方法：

1. **权威来源是客户端自带的 `arena-docs`**（`resources/app/arena-docs/index.html`）：
   页面上渲染出来的常量值就是运行时真值；
2. **再用文档 prose 里的公式交叉验证**，例如塔的"攻击 1000 起、每格 -50"只有
   `TOWER_FALLOFF_RANGE=21` 才对得上；
3. **typedings 只当参考**：`constants.d.ts` 里有个别字面值是旧的
   （例如 `TOWER_FALLOFF_RANGE` 写 20，与文档和公式都矛盾）。

## B1 `/game/visual` 是类，不是模块级函数（真机必炸）

真机上 `visual` 模块导出的是 `class Visual`，绑定库原来把它当成"带一组函数的命名空间"，
`visual.line(...)` 在真机上会直接抛
`TypeError: visual.line is not a function`。这条离线抓不到——因为 mock 一开始也是按
"命名空间函数"实现的，两边一起错。

修法（绑定层 + mock 一起改）：

- `internal/Js.java`：删掉 `visual()`，新增 `newVisual()` / `newVisual(layer)` /
  `newVisual(layer, persistent)`，脚本是 `return new visual.Visual(...)`；
- `internal/Raw.java`：`Raw.Visual` 从"模块命名空间"改成**实例接口**——
  `line/circle/text/poly/rect/clear/size` 返回 `Visual`（链式），`layer`/`persistent` 是属性；
- `screeps.arena.Visual`：重写成可实例化的类，三个构造器重载 + 链式实例方法；
- 新增 `ShapeStyle`（`poly`/`rect` 共用）、`CircleStyle.setDashed/setDotted`、
  `TextStyle.setFont/setBackgroundPadding/setStroke/setStrokeWidth`；
- `tools/arena-mock/visual.mjs`：同步改成 `export class Visual`（构造 + 链式实例方法，写进 `world.logs`）；
- `bot/probe/Main.java`：新增"new 一个实例 + 把整条链走一遍"的自检；
- `bot/spawnswamp/Main.java`：改用 `private static Visual debug` 懒创建 + `debug.line(...)`。

教训：**mock 的行为可以是近似，形状不允许是近似**。形状（是函数还是类、参数几个、
返回什么）是绑定层对真机的下注，mock 跟着下注就会"越绿越炸"。

## B2 mock 常量抄错一批

| 常量 | 真值 | mock 旧值 |
| --- | --- | --- |
| `SPAWN_HITS` | 3000 | 5000 |
| `SPAWN_ENERGY_CAPACITY` | 1000 | 300 |
| `EXTENSION_HITS` | 100 | 1000 |
| `EXTENSION_ENERGY_CAPACITY` | 100 | 50 |
| `CONTAINER_HITS` | 300 | 250 |
| `TOWER_CAPACITY` | 10 | 1000 |
| `TOWER_RANGE` | 20 | 50 |
| `TOWER_OPTIMAL_RANGE` | 1 | 5 |
| `TOWER_FALLOFF_RANGE` | 21 | 20 |
| `TOWER_FALLOFF` | 1 | 0.75 |
| `TOWER_POWER_ATTACK` | 1000 | 600 |
| `TOWER_POWER_HEAL` | 600 | 400 |
| `TOWER_POWER_REPAIR` | 200 | 800 |
| `WALL_HITS` / `RAMPART_HITS` | 10000 | 1 |
| `CREEP_SPAWN_TIME` | 3 tick/部件 | 1 tick/部件 |
| `RANGED_ATTACK_DISTANCE_RATE` | `{0:1, 1:1, 2:0.4, 3:0.1}` | 数组写错且没被用上 |
| `CONSTRUCTION_COST` | Tower 1250 / Extension 200 / Road 10（沼泽 ×5）/ Container 100 / Wall 100 / Rampart 200 / Spawn 3000 | 3000/300/300/5000/100/300/15000 |
| `ROAD_HITS`（沼泽） | 2500 | 未区分地形 |

这些值大多是 mock 早期"随手拍的近似"，后来被当真值用了。现在全部改成文档值，
并在 `constants.mjs` 顶部写明核对方法（搜客户端 docs 页面，渲染值才是真值）。

## B3 孵化时长是 3 tick/部件

文档 `StructureSpawn` 页写得很清楚：**Spawn time 3 ticks per each body part**。
mock 原来是"1 tick 一个部件"，会让离线测出来的孵化节奏比真机快 3 倍。
现在 `needTime = body.length * CREEP_SPAWN_TIME`。

顺带修了 probe 自检里一个等待条件错误：它原来用 `exists()` 判断"出壳了没有"，
而真机语义要用 `creep.spawning` 翻 false；`exists` 对孵化中的对象也可能为 true，
于是断言在第 2 tick 就提前判负。改成等 `!isSpawning()` 再检查"认领回同一实例"。

## B4 mock 行为缺口（按真机语义补齐）

- `harvest`：没有 WORK → `ERR_NO_BODYPART`；背包满时**溢出部分掉在地上**（同格合并）；
- `heal` / `rangedHeal`：没有 HEAL → `ERR_NO_BODYPART`；
- `rangedAttack` / `rangedMassAttack`：按 `RANGED_ATTACK_DISTANCE_RATE` 做距离衰减；
- `build`：没有 WORK → `ERR_NO_BODYPART`；
- `transfer`：目标满 → `ERR_FULL`，并按目标的剩余容量截断；`withdraw` / `pickup`：背包满 → `ERR_FULL`；
- 新增 `pull(target)`：目标不是 creep → `ERR_INVALID_TARGET`，不相邻 → `ERR_NOT_IN_RANGE`；
- `StructureSpawn`：`spawnCreep` 从**全部我方 spawn + extension 的能量池**扣费
  （不足 → `ERR_NOT_ENOUGH_ENERGY`）、body 空或超 50 → `ERR_INVALID_ARGS`、
  `spawning` 布尔随孵化状态翻转、`directions`/`setDirections` 真实生效；
- `StructureTower`：`attack`/`heal` 查冷却（`ERR_TIRED`）、目标合法性、射程、
  能量（扣 `TOWER_ENERGY_COST`），威力按距离线性衰减；
- 工地完工→实体建筑：进度满的那一刻工地消失、建筑出现（沼泽上的路 2500 耐久），
  且 `createConstructionSite` 超过 `MAX_CONSTRUCTION_SITES`（10）→ `ERR_FULL`；
- 结构 hits 不再硬编码，全部走常量。

## B5 绑定库缺失 API 补齐

- `GameObject.getTicksToDecay()`：没有该字段的对象返回 `-1`（区别于"下 tick 消失"的 0）；
- `Creep.isSpawning()`、`Creep.pull(Creep)`；
- `StructureSpawn.getDirections()`；
- `Store` 的三个无参重载：`getCapacity()` / `getUsedCapacity()` / `getFreeCapacity()`。

## 回归矩阵（2026-09-29）

| 套件 | 字符串 id | 数字 id |
| --- | --- | --- |
| probe（绑定库自检，19 项） | 19/19 | 19/19 |
| bots（spawn-and-swamp / harvest / ctf） | 3/3 | 3/3 |
| smoke（教程 10 关加载 + loop） | 10/10 | 10/10 |
| tutorial（教程 10 关行为断言） | 10/10 | 10/10 |

跑的是**外部 IntelliJ 工程**用新 jar 重新构建出来的产物
（`mvn install` 绑定库 → 工程 `build.cmd` → `dist/tutorial`）。

## 已知的 mock 差距（明确不装）

- `StructureSpawn` 每 tick 自动回一点能量：文档只说 "a little amount"，没有常量也没有数值，
  mock 不猜；需要靠关卡脚本自己补能量；
- `pull` 只做"把目标往拉的人身边拖一格"的最小语义，真机是引擎接管整段跟随；
- 塔/攻击/疲劳等已经对齐数值和主要规则，但引擎细节不可能全等；
- CTF 的抢旗/回旗判定要按游戏实际规则重写（属于策略层，另开条目）。

## 真机复验清单（仍未完成的一次）

1. 重建产物：仓库 `mvn package`；外部工程先 `mvn install` 新绑定库再跑 `build.cmd`；
2. 客户端代码目录指向 `dist\spawn-and-swamp`（或 `dist\capture-the-flag`），**重开一局**
   （代码是开局载入的）；
3. 判据：
   - 日志面板没有 `!!` 开头的异常，也没有 `visual.line is not a function`；
   - Spawn & Swamp 里能看到红色调试线（这条线专门验证 B1 的类形状）；
   - tick 持续增长，creep 正常孵化（`spawning` 3 tick/部件、`t=..` 日志稳定）。
