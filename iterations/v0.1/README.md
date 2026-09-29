# v0.1 —— 跑通闭环

状态：已完成（离线全过；真机教程第 1 关已跑通，第 7 关抓到的 id 类型问题已修复、待复验）

## 交付内容

- 绑定库 `screeps.arena`：`/game` 核心原型 + 全局函数 + `/game/path-finder` + `/game/visual`
- 两个示例策略：`bot.spawnswamp`（Spawn & Swamp）、`bot.ctf`（基础 Capture the Flag）
- 第三个示例策略 `bot.harvest`：从一份"自己模拟游戏"的 Java 代码搬过来的采集循环，
  只依赖 `/game` 核心原型（creep、spawn、source、store）
- 一个关卡一个产物目录：`dist/spawn-and-swamp/main.mjs`、`dist/capture-the-flag/main.mjs`、
  `dist/harvest/main.mjs`
- 绑定库可以 `mvn install` 成 jar 被外部工程依赖（第一个消费者是一个 IntelliJ 工程），
  见 `../ideas/002-api-as-jar.md`
- 离线模拟器 `tools/`：拦截 `/game/*`、`/arena` 的模块解析，用 Node 驱动 `loop()`
- `README.md`：构建、上手、API 速览、设计取舍

## 验证记录

构建（`mvn package`，JDK 17 与 25 各跑过一次，TeaVM 0.15.0，`optimizationLevel=SIMPLE`）：

| 产物 | 体积 |
| --- | --- |
| `dist/spawn-and-swamp/main.mjs` | 约 112 KB |
| `dist/capture-the-flag/main.mjs` | 约 131 KB |
| `dist/harvest/main.mjs` | 约 111 KB |

离线模拟（`node --import ./tools/register.mjs ./tools/sim.mjs <ticks> <arena>`）：

- Spawn & Swamp 跑 40 tick：孵化队列正常推进，己方 creep 追上并消灭敌方 creep，
  之后围攻敌方 spawn（5000 → 2240 血），无异常
- Capture the Flag 跑 30 tick：三种身体轮换孵化，远程单位触发了 `searchPath` 逃跑分支，
  近战单位击中敌方 creep，无异常
- `CostMatrix` + `SearchOptions` 这条不常走的路单独验证过：会正确生成
  `new pathFinder.CostMatrix()`，自定义代价确实生效（同一次寻路 path=15、cost=30）
- 加心跳日志时离线跑了一遍，抓到一个**只在真机才会炸**的 bug：`Js.arenaInfo()` 的
  `@JSBodyImport` 别名没出现在脚本里，产物留下了未定义的裸标识符 `arenaInfo`，
  `loop()` 第一 tick 就 ReferenceError。编译期完全不报，只有真跑起来才现形
- 移植 `bot.harvest` 时又抓到 mock 的一个缺口：`store` 被写成了裸 JS 对象，只有
  `energy` 属性，没有真游戏里那三个方法（`getCapacity`/`getUsedCapacity`/`getFreeCapacity`），
  而且采集不受容量限制。绑定库一调 `getFreeCapacity` 就报"不是函数"。
  已改成 `makeStore(容量, 初始值)`：creep 的容量按 CARRY 部件数算，采集超容量返回错误
- 上真机前做兼容性自检，发现一个**会让产物在部分关卡彻底加载失败**的问题：产物里带着
  `import "/arena"`，而 `/arena` 根本不是全局模块——官方关卡模板写的是
  `arena/<赛季>/<关卡>/<难度>`，只有该关卡自己有这一份。`Flag` 其实在 `/game/prototypes` 上
  （每个关卡的官方 typings 都导出它），只有 `BodyPart` 属于关卡模块。已把 `Flag` 挪回
  `/game/prototypes`、`BodyPart` 改成显式登记（`GameObjects.registerBodyPart()`），
  并在 `tools/sim.mjs` 里加静态检查：产物引用了 `/arena` 而该关卡没有这个模块，直接判失败。
  修完三个产物都不再 import `/arena`
- 用 `sim.mjs` 的第四个参数跑了一份**外部工程产出的** `main.mjs`（IntelliJ 里那个
  `cn.net.zhu.demo` 采集 bot）：40 tick 内孵化出 worker 并稳定往返搬运，spawn 能量 100 → 200；
  换到 Spawn & Swamp 场景跑 25 tick 也无异常
- **真机跑通（2026-09-29）**：把上面那份外部工程的 `main.mjs` 绑到教程关卡目录，在 Steam
  客户端（免费 Demo，appid 1658320）里实跑教程第 1 关 "Loop and import"：11 tick 后
  Victory / Passed，服务器日志面板出现产物打的日志——
  `[cn.net.zhu.demo] 已加载：Loop and import（最多 2000 tick）`、`tick=10 worker=0 累计孵化=0`。
  这是第一次在真机上确认：TeaVM 产物能被官方服务器加载、`loop()` 每 tick 都被调用、
  `Game.log` / `Game.describeArena` / `GameObjects` 在真机环境里行为正常
- **真机复测（教程第 7 关）抓到绑定库的一类 bug**：真机的 `id` 是数字，而 TeaVM 把 JS 值
  按 Java `String` 声明硬转会做出"内容是数字的假字符串"，一进 `HashMap` 就抛异常；
  孵化中的对象没有 `id` 时是另一种崩法。已系统排查同类隐患并修复（三处属性改 `JSObject`、
  无 id 对象按 JS 身份复用实例、`getObjectById` 数字兜底、强转前判类型、mock 对齐真机），
  离线回归扩成 8 组 × 两种 id 模式。详见 `../ideas/008-js-value-marshalling.md`；
  修复后的真机复验清单见该文档文末（仍欠一次实跑）
- 真机启动客户端的方式（供自动化复现）：直接跑 `screeps_arena.exe` 会卡在启动画面——
  拿不到 Steam ticket，IPC `get-steam-session-ticket` 永不返回；要用 Steam 的 appid
  环境变量和正确的工作目录启动，调试时可另加端口，例如
  `$env:SteamAppId='1658320'` 加 `Start-Process ...\screeps_arena.exe -ArgumentList '--remote-debugging-port=9222' -WorkingDirectory ...\ScreepsArenaDemo`

## 顺带确认下来的实现事实

这些是写绑定库时踩过的坑，记下来免得以后重踩：

- `@JSBodyImport` 的 `alias` 是**模块命名空间在脚本里的变量名**，不是成员名；
  脚本里要写 `utils.getTicks()` 而不是 `getTicks()`
- 而且 `alias` 必须真的出现在脚本里：TeaVM 把它绑定成脚本内的名字，脚本里没写别名，
  那个名字就是自由变量。要取模块上的导出就写 `模块别名.导出名`——`Js.arenaInfo()` 就栽在这
- TeaVM 的 JS 后端支持 lambda、方法引用、`java.util.function`、枚举 switch、`Arrays.asList`
  ——绑定库和示例代码可以照常写 Java
- 读取可能缺席的数值属性必须经过 `Js.numberOrZero` 之类的桥，直接当 `int` 读会拿到
  `undefined` 参与运算后的垃圾值
- `spawn.spawning` 在没有孵化时是 `undefined` 而不是 `null`
- `store` 是"属性 + 方法"的混合体：既能直接读 `store.energy`，也有那三个 get 方法
- 中立旗子身上根本没有 `my` 属性，"中立"要靠"属性不存在"判断
- 地图坐标在 `/game` 里是 100x100（离线 mock 按这个尺寸实现）

## 遗留问题

- ~~**真机验证还没做**~~ → 已完成一次（教程第 1 关，见上面的验证记录）。还差的是
  **竞技场实战**：免费 Demo 明确禁止联机竞技场（客户端原文 "Playing in multiplayer arenas
  is disabled in the Demo version"），要真打 Spawn & Swamp / CTF 得用完整版；教程 2~10 关
  按顺序解锁，每关都要写各自的最小策略才能过
- ~~`/arena` 根模块在非 CTF 关卡里是否一定可 import 未在真机确认~~ → 已查清：
  `/arena` 不是全局模块（见上面的验证记录），库不再引用它，产物里也不会出现
- **教程第 7 关的真机复验还没做**：id 类型问题修复后产物已重建、离线 8 组全过，
  但还没有在真机上重开第 7 关实跑过；步骤见 `../ideas/008-js-value-marshalling.md` 文末
- 关卡专属原型目前只绑了 `BodyPart`；Collect & Control 的 `ScoreCollector`、`AreaEffect`
  还没有，见 `../ideas/006-scenario-modules.md`
- `Game.getCpuTime()` 返回纳秒这个单位是照着类型定义写的，真机上待核对
- CTF 策略只是骨架：抢旗、带旗回家的判定需要按游戏实际规则重写
- 体积和 CPU 还没有优化，见 `../ideas/004-output-size-and-cpu.md`
