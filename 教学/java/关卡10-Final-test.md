# 关卡 10：Final test（期末考试）

> 游戏里这一关：顶住一波进攻、消灭所有敌方 creep，通关教程——官方**不给示例代码**。
> 这篇给你一份完整的 Java 参考实现。

## 先学 Java：本关用到的组合技能

- 分类调度：`hasBodyPart(WORK)` 当工人、`hasBodyPart(ATTACK)` 当战士，同一份 `loop` 跑两种逻辑。
- 补人状态机：工人 < 2 先补工人，再补战士；`isSpawning()` 防忙等，`ERR_NOT_ENOUGH_RESOURCES` 静默重试。
- 复用前几关：工人 = 第 08 关的采集循环；战士 = 第 03 关的“打或追”，升级成“找最近敌人”。
- 心跳日志：开局报一次、每 10 tick 报一次态势——真机调试先看这两行。

## 完整策略（可直接编译）

```java
import java.util.List;
import org.teavm.jso.JSExport;
import screeps.arena.BodyPartType;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.Game;
import screeps.arena.GameObjects;
import screeps.arena.ResourceType;
import screeps.arena.Source;
import screeps.arena.SpawnCreepResult;
import screeps.arena.Store;
import screeps.arena.StructureSpawn;

public final class Main {

    private static final List<BodyPartType> WORKER =
            List.of(BodyPartType.WORK, BodyPartType.CARRY, BodyPartType.MOVE);
    private static final List<BodyPartType> FIGHTER =
            List.of(BodyPartType.MOVE, BodyPartType.ATTACK);

    private static final int WANTED_WORKERS = 2;
    private static final int WANTED_FIGHTERS = 2;
    private static final int REPORT_INTERVAL = 10;

    @JSExport
    public static void loop() {
        report();

        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null) {
            return;
        }
        recruit(spawn);

        for (Creep creep : GameObjects.myCreeps()) {
            if (creep.hasBodyPart(BodyPartType.WORK)) {
                runWorker(creep, spawn);
            } else if (creep.hasBodyPart(BodyPartType.ATTACK)) {
                runFighter(creep);
            }
        }
    }

    /** 开局报一次关卡信息，之后定期报态势；真机排查先看这里。 */
    private static void report() {
        int tick = Game.getTicks();
        if (tick == 1) {
            Game.log("[教程终测] 已加载：" + Game.describeArena());
        }
        if (tick % REPORT_INTERVAL == 0) {
            Game.log("tick=" + tick
                    + " 我方=" + GameObjects.myCreeps().size()
                    + " 敌方=" + GameObjects.enemyCreeps().size());
        }
    }

    /** 先补工人，再补战士；能量不够就下一 tick 再试。 */
    private static void recruit(StructureSpawn spawn) {
        if (spawn.isSpawning()) {
            return;
        }
        long workers = GameObjects.myCreeps().stream()
                .filter(c -> c.hasBodyPart(BodyPartType.WORK)).count();
        long fighters = GameObjects.myCreeps().stream()
                .filter(c -> c.hasBodyPart(BodyPartType.ATTACK)).count();

        List<BodyPartType> body = null;
        if (workers < WANTED_WORKERS) {
            body = WORKER;
        } else if (fighters < WANTED_FIGHTERS) {
            body = FIGHTER;
        }
        if (body == null) {
            return;
        }
        SpawnCreepResult result = spawn.spawnCreep(body);
        if (!result.isSuccess() && result.error() != ErrorCode.ERR_NOT_ENOUGH_RESOURCES) {
            Game.log("孵化失败：" + result.error());
        }
    }

    /** 工人：背包没满去挖最近的矿，满了送回 spawn。 */
    private static void runWorker(Creep creep, StructureSpawn spawn) {
        Store store = creep.getStore();
        if (store.getFreeCapacity(ResourceType.ENERGY) > 0) {
            Source source = Game.findClosestByRange(creep, GameObjects.sources());
            if (source != null && creep.harvest(source) == ErrorCode.ERR_NOT_IN_RANGE) {
                creep.moveTo(source);
            }
        } else if (creep.transfer(spawn, ResourceType.ENERGY) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(spawn);
        }
    }

    /** 战士：找最近的敌人，贴身就打，够不着就追；没有敌人就原地待命。 */
    private static void runFighter(Creep creep) {
        Creep target = nearestEnemy(creep);
        if (target == null) {
            return;
        }
        if (creep.rangeTo(target) <= 1) {
            creep.attack(target);
        } else {
            creep.moveTo(target);
        }
    }

    private static Creep nearestEnemy(Creep creep) {
        Creep nearest = null;
        int bestRange = Integer.MAX_VALUE;
        for (Creep enemy : GameObjects.enemyCreeps()) {
            int range = creep.rangeTo(enemy);
            if (range < bestRange) {
                bestRange = range;
                nearest = enemy;
            }
        }
        return nearest;
    }
}
```

## 再看游戏：这一关要做什么

- 你有 Spawn 和能量矿；地图另一侧会来一波敌人。先出工人采能量，再出战士拦截，全灭敌人即通关。
- 这一关的节奏说明一个核心循环：**经济（采能）→ 兵力（孵化）→ 战斗（拦截）**，
  之后所有竞技场策略都是这个循环的变体。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期日志：

   ```
   [教程终测] 已加载：Final test（最多 2000 tick）
   tick=10 我方=2 敌方=3
   ...
   ```

## 坑与差异

- 敌人数量、来袭方向以实际关卡为准；`nearestEnemy` 是通用写法，不假设地图布局。
- 想打完整赛季，参考本仓库三个完整 bot：
  `bot/harvest/Main.java`（纯采集）、`bot/spawnswamp/Main.java`（孵化 + 战士 + 画线调试）、
  `bot/ctf/Main.java`（抢旗）。

[← 关卡 09](关卡09-Construction.md) | [返回目录](README.md)
