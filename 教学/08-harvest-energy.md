# 第 8 关：Harvest energy（采集能量）

> 原文：客户端 `src/mocks/tutorial/8-harvest-energy.template.html`（游戏内教程页面）。

能量资源有不同存在形式：可以存在 container 里，也可以从 Source（能量矿）采集。creep 如果有 `WORK` 身体部件，就能用 `harvest` 从矿里提取能量：

```javascript
creep.harvest(source);
```

creep 必须待在矿旁边才能采集；`WORK` 部件越多，每 tick 能采的能量越多。矿里的能量储备是无限的，你的工人能采多少就有多少。

## 本关目标

采集并运送 1000 能量到 spawn。

## 官方示例代码（JavaScript）

```javascript
import { prototypes, utils, constants } from 'game';

export function loop() {
    var creep = utils.getObjectsByPrototype(prototypes.Creep).find(i => i.my);
    var source = utils.getObjectsByPrototype(prototypes.Source)[0];
    var spawn = utils.getObjectsByPrototype(prototypes.StructureSpawn).find(i => i.my);

    if(creep.store.getFreeCapacity(constants.RESOURCE_ENERGY)) {
        if(creep.harvest(source) == constants.ERR_NOT_IN_RANGE) {
            creep.moveTo(source);
        }
    } else {
        if(creep.transfer(spawn, constants.RESOURCE_ENERGY) == constants.ERR_NOT_IN_RANGE) {
            creep.moveTo(spawn);
        }
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

```java
import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.Game;
import screeps.arena.GameObjects;
import screeps.arena.ResourceType;
import screeps.arena.Source;
import screeps.arena.Store;
import screeps.arena.StructureSpawn;

public final class Main {
    @JSExport
    public static void loop() {
        Creep creep = GameObjects.myCreeps().get(0);
        Source source = GameObjects.sources().get(0);
        StructureSpawn spawn = GameObjects.mySpawn();

        Store store = creep.getStore();
        if (store.getFreeCapacity(ResourceType.ENERGY) > 0) {
            if (creep.harvest(source) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(source);
            }
        } else {
            if (creep.transfer(spawn, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(spawn);
            }
        }
    }
}
```

- JS 里 `creep.store.getFreeCapacity(...)` 返回数值，0 是假、非 0 是真；Java 要明确写 `> 0`。
- `RESOURCE_ENERGY` → `ResourceType.ENERGY`；`ERR_NOT_IN_RANGE` → `ErrorCode.ERR_NOT_IN_RANGE`。
- 完整的两只工人采集版策略见仓库里的 `src/main/java/bot/harvest/Main.java`。

[← 第 7 关](07-spawn-creeps.md) | [返回目录](README.md) | [第 9 关 →](09-construction.md)
