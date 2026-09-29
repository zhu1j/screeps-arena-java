# 关卡 08：Harvest energy（采集能量）

> 游戏里这一关：采集并运送 1000 能量到 spawn。

## 先学 Java：本关用到的 API

- 两状态逻辑：`getFreeCapacity(...) > 0` → 去采集；否则 → 送回家。这就是最基础的**状态机**。
- `creep.harvest(source)`、`creep.transfer(spawn, ResourceType.ENERGY)` 都是“先试后走”模式。
- `GameObjects.sources()` / `GameObjects.mySpawn()` 两个快捷方法。
- `Store` 的数值比较要写清楚：`> 0`、`== 0`，不要用“真假值”。

## 完整策略（可直接编译）

```java
import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.GameObjects;
import screeps.arena.ResourceType;
import screeps.arena.Source;
import screeps.arena.Store;
import screeps.arena.StructureSpawn;

public final class Main {

    @JSExport
    public static void loop() {
        Creep creep = GameObjects.myCreeps().stream().findFirst().orElse(null);
        Source source = GameObjects.sources().stream().findFirst().orElse(null);
        StructureSpawn spawn = GameObjects.mySpawn();
        if (creep == null || source == null || spawn == null) {
            return;
        }

        Store store = creep.getStore();
        if (store.getFreeCapacity(ResourceType.ENERGY) > 0) {
            if (creep.harvest(source) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(source);
            }
        } else if (creep.transfer(spawn, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(spawn);
        }
    }
}
```

## 再看游戏：这一关要做什么

- 只有一个带 `WORK` 的工人；装满背包就送回 spawn，累计 1000 能量后通关。
- 矿里的能量无限，采集速度取决于 `WORK` 数量；一个装不满的工人是因为 `CARRY` 少，不是矿没了。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：creep 在矿和 spawn 之间往返；能量攒够后过关。

## 坑与差异

- 想让多个工人一起干？在 `loop` 里换成 `for (Creep creep : GameObjects.myCreeps())` 逐个跑这套逻辑即可。
- 完整的多工人版本（含补人逻辑）见 `src/main/java/bot/harvest/Main.java`——那里面还解释了
  “为什么装满才回、为什么用 getFreeCapacity 而不是剩多少”。

[← 关卡 07](关卡07-Spawn-creeps.md) | [返回目录](README.md) | [关卡 09 →](关卡09-Construction.md)
