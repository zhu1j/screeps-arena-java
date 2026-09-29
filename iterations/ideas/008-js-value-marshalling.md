# 008 JS 值跨界：id 是数字，不是字符串

状态：**已修复并二次加固**（绑定库 0.1.0-SNAPSHOT，2026-09-29；
离线回归 8 组 × 两种 id 模式全过，真机复验步骤见文末）

关联：这条把 `007` 的疑问彻底定性了（结论：不是 `spawnCreep` 的问题）。

## 症状

真机（安装版 v1.0.12）教程第 7 关日志：

```
t=1 creep=0 旗子=(45,48)(55,48)
!! spawnCreep 抛异常：null: (JavaScript) TypeError: Cannot read properties of undefined (reading 'length')
!! 第 7 关脚本异常：null: (JavaScript) TypeError: $this.$nativeString.charCodeAt is not a function   ← 之后每 tick
```

## 根因

TeaVM 给"返回 `String` 的 JS 属性"生成的转换是 `$rt_str(value)`：

```js
$rt_str = str => str === null ? null : jl_String__init_4(str);
jl_String__init_1 = ($this, $v) => { $this.$nativeString = $v; };
```

它只把 JS 值塞进 Java 字符串的 `$nativeString` 字段，**完全不检查类型**。
而 `GameObject.id` 的官方类型是 `number|string`，真机给的是**数字**。
于是 `GameObjects.wrap()` 拿到一个"内容是数字的字符串"，紧接着被当成 `HashMap` 的 key：

```js
jl_String_hashCode = $this => {
    ...
    if ($i >= $this.$nativeString.length)                 // (数字).length === undefined → 判断为 false
    $this.$hashCode0 = ... + $this.$nativeString.charCodeAt($i);   // Boom
```

两条报错是同一个 bug 的两种取值：

| `$nativeString` 实际是 | 崩出来的信息 |
| --- | --- |
| 数字（`id` 是数字） | `$this.$nativeString.charCodeAt is not a function` |
| `undefined`（对象还没有 id） | `Cannot read properties of undefined (reading 'length')` |

## 修法

跨界处先让 JS 自己执行一次 `String(value)`，拿到货真价实的字符串再进 Java：

```java
@JSBody(params = {"value"}, script = "return String(value);")
public static native String stringOf(JSObject value);
```

- `Raw.GameObject.id()`、`Raw.ArenaInfo.name()/season()`、`Raw.ArenaBodyPart.type()`
  从 `String` 改成 `JSObject`，需要文本的地方走 `Js.stringOf(...)`。
- 其余 `String` 类型的 JS 属性（`Creep.body[].type`、`Resource.resourceType`、
  `ConstructionSite.structurePrototypeName`）确实是字符串，暂时保持原样。

## 顺带修掉的同类问题：数组

`spawnCreep(List<BodyPartType>)` 原来靠 TeaVM 的自动数组转换（`otji_JS_wrap`），
而它是**按声明类型**逐元素 `$rt_ustr` 的：

```js
otji_JS_wrap = $array => { ... $result[$i] = $rt_ustr(var$2[$i]); ... };
```

`String[]` 恰好能转对，`setDirections(int[])` 就转出一串 `undefined`——
这解释了"调了 `setDirections` 之后地图直接不显示"。
现在两个方法都自己拼 JS 数组（`Js.newArray()` + `Js.setElement` / `Js.setNumberElement`），
不再依赖自动转换。

## 离线 mock 为什么没抓到（教训）

`tools/arena-mock/world.mjs` 的 `newId()` 生成的是 `"obj_1"` 这样的**字符串**，
正好绕开了这条路径。离线模拟器必须把"真机类型比我们假设的更宽"也模拟出来：

```powershell
node tools/tests/run.mjs "E:\Program Files (x86)\Game\ScreepsArenaDemo\ScreepsArena\dist\tutorial"
```

（`run.mjs` 会把每一套都在字符串 id 和 `MOCK_NUMERIC_IDS=1` 数字 id 两种模式下各跑一遍。）
修复前数字 id 模式 1/10 通过（`level02` 起全崩）；修复后 10/10。

**以后往 mock 里加数据形状时，按官方 typings 里最宽的那个类型写**，
不要图省事挑一个好实现的。

## 改动清单

绑定库（`io.github.screepsarena:screeps-arena-java`，改完要 `mvn install` 到本地仓库）：

- `internal/Js.java`：新增 `stringOf` / `setElement` / `setNumberElement`
- `internal/Raw.java`：`GameObject.id()`、`ArenaInfo.name()/season()`、`ArenaBodyPart.type()` 改为 `JSObject`；
  `StructureSpawn.spawnCreep(JSObject)`、`setDirections(JSObject)`
- `GameObjects.wrap()`、`GameObject.getId()`、`ArenaInfo`、`BodyPart`：改用 `Js.stringOf`
- `StructureSpawn`：两个方法自建 JS 数组

工具：

- `tools/arena-mock/world.mjs`：新增 `MOCK_NUMERIC_IDS=1` 开关

## 二次加固（第一轮修复后又系统查了一遍同类问题）

第一轮只修了"`id` 被当字符串"和 `setDirections` 的自动数组转换，但同一种病——
**把 JS 值按 Java 声明的类型硬转，类型不符就做出坏对象**——在库里还有别的入口。
这一轮把它们统一清了，并且把"孵化中的对象没有 id"这个真机行为补进了 mock 和测试。

### 还有三处 JS 属性不该固定当 `String` 读

`id` 之外，官方 typings 里还有若干属性的类型是"字面量联合类型"，真机给哪个值、
字段在不在，都不该由绑定层的 Java 签名提前拍板。这三处从 `String` 改成 `JSObject`，
需要文本时走 `Js.stringOf(...)`：

- `Raw.BodyPartEntry.type()`（creep 身体部件）
- `Raw.ConstructionSite.structurePrototypeName()`（工地要建的原型名；
  这个字段官方 typings 甚至没声明，但真机有、客户端 UI 在读）
- `Raw.Resource.resourceType()`（掉落资源）

### `stringOf` 对 `undefined` 的处理

第一版的 `String(value)` 会把 `undefined` 变成字面量 `"undefined"` 字符串。
对 id 来说这是灾难：所有"还没有 id"的对象（孵化中的 creep）会算出同一个缓存键、
互相认错。现在 `stringOf` 先判 nullish，`undefined`/`null` 直接返回 Java 的 `null`，
"没有"这个含义被完整保留。

### 没有 id 的对象：按 JS 对象身份复用实例

`GameObjects.wrap()` 原来把所有对象都往 `CACHE`（`id -> 实例`）里塞。
孵化中的 creep 没有 id，只能靠"是不是同一个 JS 对象"识别。现在多了一张
`UNIDENTIFIED` 列表：没有 id 的对象在这里按 JS `===` 找到同一个实例；
孵化完成、对象有 id 之后，从列表里"认领转正"进 `CACHE`——
整场比赛里同一只 creep 始终是同一个 Java 实例，`==` 语义不破。

### `getObjectById` 的数字兜底

官方 typings 说参数是 `string`，但对象的 `id` 实际是 `number|string`。
真机的 `getObjectById` 如果按 `===` 比较，拿字符串形式的数字 id 就查不到。
现在 `Js.objectById` 先按原样查，查不到再转成数字查一次
（`MOCK_NUMERIC_IDS=1` 的 mock 故意把 `getObjectById` 实现成最严格的 `===`，
专门用来验证这条兜底）。

### 强转之前先看类型

`StructureSpawn.spawnCreep()`、`Game.createConstructionSite()`、`Spawning.creep()`
原来拿到 JS 对象就直接转成具体包装类；对象形状不对时，这个强转会让整个 tick 抛异常。
现在先 `instanceof` 判断，认不出来就返回"没拿到对象"（`null` / 失败结果），
一次探测失败不会打断整局。

### mock 和测试补上了真机行为

- `tools/arena-mock/prototypes.mjs`：`spawnCreep` 之后孵化中的 creep **id 就是
  `undefined`**，孵化完成才分配；这正是真机第二条报错
  （`Cannot read properties of undefined (reading 'length')`）的来源
- `tools/tests/`：离线回归收进仓库
  - `probe.mjs` 驱动自检 bot `bot.probe.Main`（`dist/probe/main.mjs`），
    断言 11 项：id 读写、`getObjectById` 同实例、孵化中无 id、实例复用/认领、
    工地原型名、身体部件、资源类型、旗子三态
  - `bots.mjs`：三个示例 bot 各跑 300 tick
  - `smoke.mjs` / `tutorial.mjs`：教程 10 关的加载与行为断言
  - `run.mjs`：总入口，每一套都在"字符串 id"和"数字 id"两种模式各跑一遍

### 回归矩阵（2026-09-29，预演构建与外部 IntelliJ 工程产物各跑一遍）

| 套件 | 字符串 id | 数字 id |
| --- | --- | --- |
| probe（绑定库自检） | 11/11 | 11/11 |
| bots（spawn-and-swamp / harvest / ctf） | 3/3 | 3/3 |
| smoke（教程 10 关加载 + loop） | 10/10 | 10/10 |
| tutorial（教程 10 关行为断言） | 10/10 | 10/10 |

数字 id 模式下修复前是 1/10——第 2 关起全崩，报错与真机一字不差。

## 真机复验（仍未完成的一次）

离线全过只证明"逻辑和这层 ABI 对了"，证明不了安装版引擎的实际行为。按这个清单复验：

1. 重建产物：仓库里 `mvn package`；外部 IntelliJ 工程先 `mvn install` 新绑定库再
   跑工程的 `build.cmd`。确认 `dist\tutorial\level07\main.mjs` 的时间戳是刚生成的。
2. 进游戏**重开第 7 关**（代码是开局载入的，必须重开关卡，不是重开客户端）。
3. 判据：日志里不再出现 `!!`；tick 计数持续增长；`t=.. creep=N` 里的 creep 会孵化并增加。
   第一条报错 `$nativeString.charCodeAt is not a function` 与第二条
   `Cannot read properties of undefined (reading 'length')` 都应当消失。
4. 可选：把代码目录临时指向 `dist\probe`（自检 bot）跑一局，日志里的 `probe:` 行
   应当与离线一致（孵化中 `id=null`、`adopted=yes`）。
5. 第 10 关的敌人 AI 属于策略强弱问题，仍要实战过关才算数。
