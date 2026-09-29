# 第 9 关：Construction（建造）

> 原文：客户端 `src/mocks/tutorial/9-construction.template.html`（游戏内教程页面）。

你不仅能孵化 creep，还能**建造建筑**！只需要一点能量和一个工人 creep 就能完成。

第一步：用 createConstructionSite 函数在游戏里放置一个 ConstructionSite（工地），需要指定坐标和未来建筑的原型类型：

```javascript
import { createConstructionSite } from 'game/utils';
import { StructureTower } from 'game/prototypes';

export function loop() {
    var constructionSite = createConstructionSite({x: 50, y: 55}, StructureTower).object;
}
```

然后让装了能量的 creep 靠近工地并 `build`：

```javascript
creep.build(constructionSite);
```

每次成功的建造都会消耗 creep `store` 里的能量，所以要在家里的能量来源和工地之间来回跑。建完之后，新建筑就会出现。

## 本关目标

建造一座 Tower。

## 官方示例代码（JavaScript）

```javascript
import { prototypes, utils } from 'game';
import { RESOURCE_ENERGY, ERR_NOT_IN_RANGE } from 'game/constants';

export function loop() {
    const creep = utils.getObjectsByPrototype(prototypes.Creep).find(i => i.my);
    if(!creep.store[RESOURCE_ENERGY]) {
        const container = utils.findClosestByPath(creep, utils.getObjectsByPrototype(prototypes.StructureContainer));
        if(creep.withdraw(container, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            creep.moveTo(container);
        }
    } else {
        const constructionSite = utils.getObjectsByPrototype(prototypes.ConstructionSite).find(i => i.my);
        if(!constructionSite) {
            utils.createConstructionSite(50,55, prototypes.StructureTower);
        } else {
            if(creep.build(constructionSite) == ERR_NOT_IN_RANGE) {
                creep.moveTo(constructionSite);
            }
        }
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

```java
import org.teavm.jso.JSExport;
import screeps.arena.ConstructionSite;
import screeps.arena.ConstructionSiteResult;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.Game;
import screeps.arena.GameObjects;
import screeps.arena.Point;
import screeps.arena.ResourceType;
import screeps.arena.StructureContainer;
import screeps.arena.StructureType;

public final class Main {
    @JSExport
    public static void loop() {
        Creep creep = GameObjects.myCreeps().get(0);
        if (creep.getStore().amount(ResourceType.ENERGY) == 0) {
            StructureContainer container =
                    Game.findClosestByPath(creep, GameObjects.of(StructureContainer.class));
            if (creep.withdraw(container, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(container);
            }
        } else {
            ConstructionSite site = Game.getObjectsByPrototype(ConstructionSite.class).stream()
                    .filter(ConstructionSite::isMine).findFirst().orElse(null);
            if (site == null) {
                ConstructionSiteResult result =
                        Game.createConstructionSite(Point.of(50, 55), StructureType.TOWER);
                if (!result.isSuccess()) {
                    Game.log("放置工地失败：" + result.error());
                }
            } else {
                if (creep.build(site) == ErrorCode.ERR_NOT_IN_RANGE) {
                    creep.moveTo(site);
                }
            }
        }
    }
}
```

- `createConstructionSite({x, y}, StructureTower)` → `Game.createConstructionSite(Point.of(50, 55), StructureType.TOWER)`，返回 `ConstructionSiteResult`（`.isSuccess()` / `.site()` / `.error()`）。
- Java 没有“原型构造函数”这种值，绑定库用 `StructureType` 枚举代替（`TOWER`、`EXTENSION`、`ROAD`、`CONTAINER`、`WALL`、`RAMPART`、`SPAWN`）。
- `store[RESOURCE_ENERGY]` 的真值判断 → `getStore().amount(ResourceType.ENERGY) == 0`。

[← 第 8 关](08-harvest-energy.md) | [返回目录](README.md) | [第 10 关 →](10-final-test.md)
