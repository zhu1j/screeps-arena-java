# 关卡 09：Construction（建造）

> 游戏里这一关：建一座 Tower。

## 先学 Java：本关用到的 API

- `Point.of(x, y)` 造位置；`Game.createConstructionSite(Point, StructureType)` 放置工地。
- `StructureType` 是枚举（`TOWER`、`EXTENSION`、`ROAD`、`CONTAINER`、`WALL`、`RAMPART`、`SPAWN`）——
  官方 JS 传的是“原型构造函数”，Java 里没有这个值，用枚举代替。
- `ConstructionSiteResult`：`.isSuccess()` 判断成功，`.site()` 拿工地，`.error()` 拿错误码。
- `Game.findClosestByPath(creep, GameObjects.of(StructureContainer.class))` —— 找最近的能量来源。
- `stream().anyMatch(...)` 判断某个东西已存在，避免重复放工地。

## 完整策略（可直接编译）

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
import screeps.arena.StructureTower;
import screeps.arena.StructureType;

public final class Main {

    @JSExport
    public static void loop() {
        Creep creep = GameObjects.myCreeps().stream().findFirst().orElse(null);
        if (creep == null) {
            return;
        }
        if (creep.getStore().amount(ResourceType.ENERGY) == 0) {
            withdrawEnergy(creep);
        } else {
            ConstructionSite site = mySite();
            if (site == null) {
                placeSite();
            } else if (creep.build(site) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(site);
            }
        }
    }

    private static void withdrawEnergy(Creep creep) {
        StructureContainer container =
                Game.findClosestByPath(creep, GameObjects.of(StructureContainer.class));
        if (container != null
                && creep.withdraw(container, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(container);
        }
    }

    private static ConstructionSite mySite() {
        return Game.getObjectsByPrototype(ConstructionSite.class).stream()
                .filter(ConstructionSite::isMine)
                .findFirst()
                .orElse(null);
    }

    /** 放工地；已经有 Tower 就不重复放。 */
    private static void placeSite() {
        boolean towerExists = Game.getObjectsByPrototype(StructureTower.class).stream()
                .anyMatch(StructureTower::isMine);
        if (towerExists) {
            return;
        }
        ConstructionSiteResult result =
                Game.createConstructionSite(Point.of(50, 55), StructureType.TOWER);
        if (!result.isSuccess()) {
            Game.log("放置工地失败：" + result.error());
        }
    }
}
```

## 再看游戏：这一关要做什么

- 官方样例在 (50, 55) 放 Tower 工地；creep 有能量就去敲工地，敲满进度后 Tower 建成、本关通过。
- 建造会持续消耗能量，所以“取能量 → 敲工地”要能持续循环，直到 `ConstructionSite` 消失（变成 Tower）。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：先出现工地，creep 来回取能量建造，Tower 落成后过关。

## 坑与差异

- 官方样例的 `if(!constructionSite)` 会到处放工地；本样例加了“已有 Tower 就不再放”的判断——真实对局里乱放工地会浪费资源。
- 工地坐标在别的关卡不一定合适；正式策略里通常用 `Game.findClosestByPath` 或按地图规划选点。

[← 关卡 08](关卡08-Harvest-energy.md) | [返回目录](README.md) | [关卡 10 →](关卡10-Final-test.md)
