# 第 5 关：Store and transfer（储物与搬运）

> 原文：客户端 `src/mocks/tutorial/5-store-and-transfer.template.html`（游戏内教程页面）。

不同的竞技场里有不同的资源，但有一种通用资源：`RESOURCE_ENERGY`（能量）。建造结构、孵化 creep、以及各种活动都要用它。这一关我们用能量给 Tower 充能，从远处消灭敌人。Tower 最远能打 20 格，但效率随距离下降。

```javascript
tower.attack(target);
```

每次射击消耗它 `store` 里的 10 点能量。给 Tower 装能量要用 creep 的 `transfer`：

```javascript
creep.transfer(tower, RESOURCE_ENERGY);
```

能量从哪来？附近有一个 Container，它的 `store` 里有能量。让 creep 靠近并用 `withdraw` 取出来：

```javascript
creep.withdraw(container, RESOURCE_ENERGY);
```

Tower 充好能量后，攻击并摧毁目标。

如果你用官方示例代码（完全没问题），会注意到这一关的导入方式变了：不再逐个导入，而是从根模块 `game` 导入整个命名空间。哪种方式更顺手，由你自己决定。

## 本关目标

消灭敌方 creep。

## 官方示例代码（JavaScript）

```javascript
import { prototypes, utils, constants } from 'game';

export function loop() {
    const tower = utils.getObjectsByPrototype(prototypes.StructureTower)[0];
    if(tower.store[constants.RESOURCE_ENERGY] < 10) {
        var myCreep = utils.getObjectsByPrototype(prototypes.Creep).find(creep => creep.my);
        if(myCreep.store[constants.RESOURCE_ENERGY] == 0) {
            var container = utils.getObjectsByPrototype(prototypes.StructureContainer)[0];
            myCreep.withdraw(container, constants.RESOURCE_ENERGY);
        } else {
            myCreep.transfer(tower, constants.RESOURCE_ENERGY);
        }
    } else {
        var target = utils.getObjectsByPrototype(prototypes.Creep).find(creep => !creep.my);
        tower.attack(target);
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

```java
import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.Game;
import screeps.arena.GameObjects;
import screeps.arena.ResourceType;
import screeps.arena.StructureContainer;
import screeps.arena.StructureTower;

public final class Main {
    @JSExport
    public static void loop() {
        StructureTower tower = Game.getObjectsByPrototype(StructureTower.class).get(0);
        if (tower.getStore().amount(ResourceType.ENERGY) < 10) {
            Creep myCreep = GameObjects.myCreeps().get(0);
            if (myCreep.getStore().amount(ResourceType.ENERGY) == 0) {
                StructureContainer container =
                        Game.getObjectsByPrototype(StructureContainer.class).get(0);
                myCreep.withdraw(container, ResourceType.ENERGY);
            } else {
                myCreep.transfer(tower, ResourceType.ENERGY);
            }
        } else {
            Creep target = GameObjects.enemyCreeps().get(0);
            tower.attack(target);
        }
    }
}
```

- `tower.store[RESOURCE_ENERGY]` → `tower.getStore().amount(ResourceType.ENERGY)`。
- 官方的“整包导入”在 Java 里对应 `import screeps.arena.*;`；不过 IDE 一般会自动导入具体类，两种都行。
- `tower.attack(target)` 返回 `ErrorCode`（`OK` 表示成功）；示例没有检查返回值。

[← 第 4 关](04-creeps-bodies.md) | [返回目录](README.md) | [第 6 关 →](06-terrain.md)
