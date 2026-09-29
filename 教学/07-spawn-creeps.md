# 第 7 关：Spawn creeps（孵化 creep）

> 原文：客户端 `src/mocks/tutorial/7-spawn-creeps.template.html`（游戏内教程页面）。

在有 Spawn（孵化场）结构的竞技场里，你可以用 `spawnCreep` 方法孵化新的 creep。新 creep 的身体要定义成身体部件数组。creep 越大，消耗 Spawn `store` 里的能量越多，费用如下：

- MOVE —— 50 能量
- WORK —— 100 能量
- CARRY —— 50 能量
- ATTACK —— 80 能量
- RANGED_ATTACK —— 150 能量
- HEAL —— 250 能量
- TOUGH —— 10 能量

`spawnCreep` 返回一个 Creep 实例，可以保存起来以后使用：

```javascript
var creep = mySpawn.spawnCreep([MOVE, ATTACK]).object;
```

也可以在 `loop` 函数外定义变量，让它在 tick 之间保持：

```javascript
var creep;
export function loop() {
    if(!creep) {
        creep = mySpawn.spawnCreep([MOVE, ATTACK]).object;
    }
}
```

注意：你可以给这个 `creep` 对象挂任意属性，用来记录目标、职责等信息：

```javascript
creep.target = flag;
```

本关有一个 Spawn 和两面旗子。

## 本关目标

孵化两个 creep，让它们分别走到不同的旗子上。

## 官方示例代码（JavaScript）

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep, Flag, StructureSpawn } from 'game/prototypes';
import { MOVE } from 'game/constants';

var creep1, creep2;

export function loop() {
    var mySpawn = getObjectsByPrototype(StructureSpawn)[0];
    var flags = getObjectsByPrototype(Flag);

    if(!creep1) {
        creep1 = mySpawn.spawnCreep([MOVE]).object;
    } else {
        creep1.moveTo(flags[0]);

        if(!creep2) {
            creep2 = mySpawn.spawnCreep([MOVE]).object;
        } else {
            creep2.moveTo(flags[1]);
        }
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

```java
import java.util.List;
import org.teavm.jso.JSExport;
import screeps.arena.BodyPartType;
import screeps.arena.Creep;
import screeps.arena.Flag;
import screeps.arena.GameObjects;
import screeps.arena.StructureSpawn;

public final class Main {
    private static Creep creep1;
    private static Creep creep2;

    @JSExport
    public static void loop() {
        StructureSpawn mySpawn = GameObjects.mySpawn();
        List<Flag> flags = GameObjects.flags();
        if (creep1 == null) {
            creep1 = mySpawn.spawnCreep(List.of(BodyPartType.MOVE)).creep();
        } else {
            creep1.moveTo(flags.get(0));
            if (creep2 == null) {
                creep2 = mySpawn.spawnCreep(List.of(BodyPartType.MOVE)).creep();
            } else {
                creep2.moveTo(flags.get(1));
            }
        }
    }
}
```

- 跨 tick 保存的变量：官方写在模块顶层；Java 用类的 `static` 字段，效果一样。
- `spawnCreep([MOVE]).object` → `spawn.spawnCreep(List.of(BodyPartType.MOVE))`，返回 `SpawnCreepResult`，用 `.creep()` 取孵化出的 creep、`.error()` 看错误码。
- 官方给 creep 挂自定义属性（`creep.target = flag`）；Java 里推荐 `static Map<String, Flag>`，用 `creep.getId()` 当键。
- 孵化中的 spawn 再次调用 `spawnCreep` 会得到 `ERR_BUSY`，可以先 `spawn.isSpawning()` 判断。

[← 第 6 关](06-terrain.md) | [返回目录](README.md) | [第 8 关 →](08-harvest-energy.md)
