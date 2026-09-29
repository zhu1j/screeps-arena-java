# 关卡 05：Store and transfer（储物与搬运）

> 游戏里这一关：给 Tower 充能，用 Tower 远程消灭敌人。

## 先学 Java：本关用到的 API

- `Store`：`amount(ResourceType)`、`getFreeCapacity(ResourceType)`、`getUsedCapacity(...)`，返回 `int`。
  - 官方 JS 写 `tower.store[RESOURCE_ENERGY]`；Java 里是 `tower.getStore().amount(ResourceType.ENERGY)`。
- `ResourceType.ENERGY` —— 资源枚举（v0.1 只有能量）。
- `ErrorCode.ERR_NOT_IN_RANGE` 的判断模式：**动作先试，够不着再走**。
- 多分支决策用 `if / else`；节点拆成 `refillTower(...)` 这类小方法。

## 完整策略（可直接编译）

```java
import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.Game;
import screeps.arena.GameObjects;
import screeps.arena.ResourceType;
import screeps.arena.StructureContainer;
import screeps.arena.StructureTower;

public final class Main {

    /** Tower 开一炮消耗的能量。 */
    private static final int SHOT_COST = 10;

    @JSExport
    public static void loop() {
        StructureTower tower = Game.getObjectsByPrototype(StructureTower.class).get(0);
        if (tower.getStore().amount(ResourceType.ENERGY) < SHOT_COST) {
            refillTower(tower);
        } else {
            Creep target = GameObjects.enemyCreeps().stream().findFirst().orElse(null);
            if (target != null) {
                tower.attack(target);
            }
        }
    }

    /** 背包空 → 去 Container 取；有货 → 送回 Tower。 */
    private static void refillTower(StructureTower tower) {
        Creep creep = GameObjects.myCreeps().stream().findFirst().orElse(null);
        if (creep == null) {
            return;
        }
        if (creep.getStore().amount(ResourceType.ENERGY) == 0) {
            StructureContainer container =
                    Game.getObjectsByPrototype(StructureContainer.class).get(0);
            if (creep.withdraw(container, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(container);
            }
        } else if (creep.transfer(tower, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(tower);
        }
    }
}
```

## 再看游戏：这一关要做什么

- Container 里有能量，Tower 射程 20 格；creep 取能量 → 交给 Tower → Tower 打敌人。
- 每次射击耗 10 能量；能量不够时 Tower 不会开火，所以“补能”和“开火”要分成两个分支。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：creep 在 Container 和 Tower 之间来回搬运，Tower 把敌人打掉后本关通过。

## 坑与差异

- JS 里 `store[...]` 可以当“有没有”用（`if(tower.store[...])`）；Java 拿的是 `int`，该比 `0` 就比 `0`、该比 `10` 就比 `10`，别写 `if (amount)`。
- 官方示例不检查 `tower.attack` 的返回值；如果塔被拆了或者目标死了，`attack` 会返回错误码，不致命。

[← 关卡 04](关卡04-Creeps-bodies.md) | [返回目录](README.md) | [关卡 06 →](关卡06-Terrain.md)
