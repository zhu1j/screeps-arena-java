# 007 spawnCreep 返回值的健壮化

状态：**已解决** · 见 `008-js-value-marshalling.md`

> 结案说明：本文下面的猜测都不成立。真凶是绑定库把 JS 的**数字 id** 塞进了 Java 字符串，
> 导致 `GameObjects.wrap()` 的 `HashMap` 一哈希就抛异常——`spawnCreep` 只是恰好第一个撞上。
> 绕过的关卡写法保留（它本身是对的：孵化结果本来就不该跨 tick 缓存），但根因在 008。

## 背景

真机上玩教程第 7 关时观察到：spawn 一直在孵化，但 creep 站在出生点不动；
外围八个落点占满之后彻底卡死（`spawnCreep` 只能拿到 `ERR_BUSY`）。

离线 mock 里同样的代码能过，说明问题出在**真机才有的差异**上。
当时的关卡代码把 `spawnCreep(...).creep()` 存进静态字段，用 `creep1 == null` 判断"该不该孵化"：
一旦这个返回值拿不到有效对象，孵化分支就永远成立，移动分支一个 tick 都跑不到——症状与观察一致。

## 可能的原因（都没定论，需要真机实验）

`StructureSpawn.spawnCreep` 的绑定实现里有三处对真机行为做了假设：

1. `GameObjects.wrap()` 用 `String id = object.id();` 当缓存键。
   官方 typings 写的是 `id: number|string`，而绑定层声明成 `String`；
   如果引擎给的是数字（或孵化中的对象还没有 id），这条路径可能抛异常。
2. `GameObjects.detect()` 靠 `instanceof prototypes.Creep` 识别类型；
   识别不出来时会退化成基础 `GameObject`，随后 `(Creep)` 强转在产物里是
   `$rt_castToClass(...)`——**严格模式下会抛 ClassCastException**，整个 tick 中断。
3. 失败时只读 `result.object`；也可以退一步从 `spawn.spawning.creep` 取（官方 `Spawning.creep` 字段）。

## 想法

- `wrap()`：先把 id 规整成字符串（`String(object.id)`，`undefined/null` 就不进缓存），
  再查/写缓存；数字 id 也能用。
- `spawnCreep()`：不要硬转类型。包完对象先 `instanceof Creep` 判断，
  认不出来就当作"这次没拿到 creep"处理，并且在结果里保留原始 `error`/对象，方便 Bot 自己决定。
- 可选：`SpawnCreepResult` 增加 `error()` 之外的 `spawning()` 视图，暴露 `needTime/remainingTime`。

## 怎么验证（真机，一次就能定性）

在任意关卡里跑一段只做诊断的 `loop()`，把下面几行打进日志面板：

```java
SpawnCreepResult result = spawn.spawnCreep(List.of(BodyPartType.MOVE));
Game.log("result=" + result + " ok=" + result.isSuccess() + " err=" + result.error());
Spawning spawning = spawn.getSpawning();
if (spawning != null) {
    Game.log("spawning needTime=" + spawning.getNeedTime() + " creep=" + spawning.creep());
}
Game.log("场上 creep 数=" + GameObjects.myCreeps().size());
```

三种可能的读数，对应三种修法：

| 日志表现 | 结论 | 修法 |
| --- | --- | --- |
| 每次都是 `ok=false err=OK`，且 `creep` 为 null | `result.object` 读不到 | 退回 `spawning.creep` |
| 日志面板直接空白 / 有异常字样 | `wrap()` 或强转抛异常中断了 tick | 按上面第 1、2 条改 `wrap()` 与 `spawnCreep()` |
| `ok=true`，但 creep 不动 | 返回值没问题，问题在别处（例如出生那一 tick 还不能动） | 忽略返回值，按"数场上数量"驱动 |

## 已经做了的绕过方案

教程第 7 关的 `level07/Main.java` 改成**完全不依赖孵化返回值**：

- 每 tick 先 `GameObjects.myCreeps()` 驱动场上 creep 去旗子（`moveTo` + `findClosestByPath` 认领），
- 再按"场上现有几只"决定补兵，并用 `spawn.isSpawning()` 挡住重复下单。

这样即使 `spawnCreep` 的结果读不出来，关卡依然能过；
但**孵化返回值本身该修**——`spawn-and-swamp`、`Capture the Flag` 这些 bot 迟早要用它。
