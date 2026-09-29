# 02 · API 速查（游戏概念 ↔ Java）

> 完整签名以 `src/main/java/screeps/arena/` 源码为准，这里只列常用部分。

## 全局：`Game`

| 方法 | 说明 | 官方 JS |
|------|------|---------|
| `Game.getTicks()` | 当前 tick | `getTicks()` |
| `Game.getCpuTime()` / `Game.getCpuTimeMillis()` | 已用 CPU 时间 | `getCpuTime()` |
| `Game.log(String)` | 写日志面板 | `console.log(...)` |
| `Game.describeArena()` | 关卡描述（日志里好用） | — |
| `Game.getObjectsByPrototype(Class<T>)` | 按类型查对象，返回 `List<T>` | `getObjectsByPrototype(...)` |
| `Game.getObjects()` | 当前全部对象 | `getObjects()` |
| `Game.getObjectById(String)` | 按 id 查 | `getObjectById(...)` |
| `Game.findInRange(Position, List<T>, int)` | 范围内筛选 | `findInRange(...)` |
| `Game.findClosestByRange(Position, List<T>)` | 距离最近 | `findClosestByRange(...)` |
| `Game.findClosestByPath(Position, List<T>)` | 按路径最近 | `findClosestByPath(...)` |
| `Game.getRange(Position, Position)` | 切比雪夫距离 | `getRange(a, b)` |
| `Game.getDirection(int dx, int dy)` / `(Position, Position)` | 求方向 | `getDirection(...)` |
| `Game.getTerrainAt(Position)` | 地形 | `getTerrainAt(...)` |
| `Game.createConstructionSite(Position, StructureType)` | 放置工地 | `createConstructionSite(...)` |

## 查找快捷方式：`GameObjects`

| 方法 | 返回 |
|------|------|
| `GameObjects.myCreeps()` / `enemyCreeps()` / `creeps()` | `List<Creep>` |
| `GameObjects.mySpawn()` / `enemySpawn()` / `spawns()` | `StructureSpawn` / `List<StructureSpawn>` |
| `GameObjects.flags()` | `List<Flag>` |
| `GameObjects.sources()` | `List<Source>` |
| `GameObjects.of(Class<T>)` | `List<T>`（等价于 `Game.getObjectsByPrototype`） |
| `GameObjects.registerBodyPart()` | 登记 `/arena` 的 `BodyPart` 原型（会掉身体部件的关卡才需要） |

## 对象包装类

### `GameObject`（所有对象的基类）

`getId()`、`getX()` / `getY()`、`exists()`、`rangeTo(Position)`、`raw()`（拿底层 JS 对象，进阶用）。

### `Creep`

| 方法 | 说明 |
|------|------|
| `isMine()` | 是不是自己的 |
| `moveTo(Position)` | 朝目标走一步 |
| `attack(GameObject)` / `rangedAttack(GameObject)` | 近战 / 远程攻击 |
| `heal(Creep)` | 治疗 |
| `harvest(Source)` | 采集 |
| `transfer(GameObject, ResourceType[, amount])` | 交出资源 |
| `withdraw(GameObject, ResourceType[, amount])` | 取出资源 |
| `build(ConstructionSite)` | 建造 |
| `drop(...)` / `pickup(Resource)` | 丢下 / 捡起 |
| `getStore()` | 背包（`Store`） |
| `getBody()` | `List<BodyPartStatus>`；想快速判断用 `hasBodyPart(BodyPartType)` |
| `getHits()` / `getHitsMax()` / `getFatigue()` | 状态数值 |

以上动作大多返回 `ErrorCode`，`ErrorCode.OK` 表示成功。

### `Store`

`amount(ResourceType)`、`getCapacity(...)`、`getUsedCapacity(...)`、`getFreeCapacity(...)`——全部返回 `int`。

### 建筑

- `StructureSpawn`：`spawnCreep(List<BodyPartType>)` → `SpawnCreepResult`（`.creep()` / `.error()` / `.isSuccess()`）、`isSpawning()`
- `StructureTower`：`attack(GameObject)`、`heal(Creep)`、`getStore()`、`isReady()`
- `StructureContainer`：`getStore()`
- `ConstructionSite`：`isMine()`、`getProgress()` / `getProgressTotal()`、`remove()`

## 枚举常量

| 枚举 | 常用值 |
|------|--------|
| `ErrorCode` | `OK`、`ERR_NOT_IN_RANGE`、`ERR_NOT_ENOUGH_RESOURCES`、`ERR_BUSY`…… |
| `BodyPartType` | `MOVE`、`WORK`、`CARRY`、`ATTACK`、`RANGED_ATTACK`、`HEAL`、`TOUGH` |
| `ResourceType` | `ENERGY` |
| `StructureType` | `TOWER`、`EXTENSION`、`ROAD`、`CONTAINER`、`WALL`、`RAMPART`、`SPAWN` |
| `Direction` | `TOP`、`TOP_RIGHT`、`RIGHT`、`BOTTOM_RIGHT`、`BOTTOM`、`BOTTOM_LEFT`、`LEFT`、`TOP_LEFT` |
| `Terrain` | `PLAIN`、`WALL`、`SWAMP`（道路是建筑，不是地形） |

## 常用代码模式

**筛我的 creep**：

```java
List<Creep> mine = Game.getObjectsByPrototype(Creep.class).stream()
        .filter(Creep::isMine)
        .toList();
```

**走到射程内再打**：

```java
if (creep.attack(target) == ErrorCode.ERR_NOT_IN_RANGE) {
    creep.moveTo(target);
}
```

**背包判断**：

```java
Store store = creep.getStore();
if (store.getFreeCapacity(ResourceType.ENERGY) > 0) {
    // 还有空位：去采集
} else {
    // 满了：送回去
}
```

**孵化并拿到结果**：

```java
SpawnCreepResult result = spawn.spawnCreep(List.of(BodyPartType.MOVE, BodyPartType.ATTACK));
if (result.isSuccess()) {
    Creep born = result.creep();
} else {
    Game.log("孵化失败：" + result.error());
}
```

**跨 tick 状态**：

```java
private static final int WANTED_WORKERS = 2;
private static Creep scout;   // 第一次 loop 里赋值，之后一直有效
```

**每 N tick 打一行日志**：

```java
if (Game.getTicks() % 10 == 0) {
    Game.log("tick=" + Game.getTicks() + " 我方=" + GameObjects.myCreeps().size());
}
```

## 位置与方向

- `Point.of(x, y)` 造一个位置；`GameObject` 本身就实现 `Position`，可以直接传给 `moveTo`、`rangeTo`。
- `a.rangeTo(b)` 是切比雪夫距离，和游戏里的 `getRange` 一致。
- 方向：`Game.getDirection(dx, dy)`、`Game.getDirection(from, to)`。

## 调试画线（进阶）

`Visual` 是个类：先 `new Visual()`（可选 `new Visual(layer)`、`new Visual(layer, persistent)`），
再在实例上链式画图。例如 `visual.line(from, to, LineStyle.create().setColor("#ff5555"))`
可以在游戏画面里画一条线；`circle` / `text` / `poly` / `rect` 用法相同，
用来观察“谁在追谁”，见 `bot/spawnswamp/Main.java`。
