package bot.harvest;

import java.util.Arrays;
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

/**
 * 采集型 bot：能量空了去挖矿，背包装满送回 spawn，循环。
 *
 * <p>这套策略是从一份"自己模拟游戏"的 Java 代码搬过来的：原来那份里 {@code Game}、
 * {@code Spawn}、{@code Creep} 都是自己写的假对象，靠 {@code main()} 的 for 循环推 100 个 tick。
 * 搬到真游戏里，换掉的只是"引擎"这一层，决策逻辑本身没动：
 *
 * <ul>
 *   <li>自己 {@code new Spawn()} → 用 {@link GameObjects#mySpawn()} 从对局里查</li>
 *   <li>自己推 tick 循环 → 游戏每 tick 调一次 {@link #loop()}</li>
 *   <li>{@code System.out.printf} → {@link Game#log}，真机上才看得到</li>
 * </ul>
 *
 * <p>另外改了两处，注释里都写了原因。
 */
public final class Main {

    private Main() {
    }

    /** 一只 worker：WORK 挖、CARRY 背、MOVE 走。 */
    private static final List<BodyPartType> WORKER =
            Arrays.asList(BodyPartType.WORK, BodyPartType.CARRY, BodyPartType.MOVE);

    /** 想维持的 worker 数量。 */
    private static final int WANTED_WORKERS = 2;

    /** 每隔多少 tick 打一行状态。真机调试时先看它有没有出现。 */
    private static final int REPORT_INTERVAL = 10;

    @JSExport
    public static void loop() {
        report();

        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null) {
            // 第一 tick 之前游戏对象还没铺开，正常不会走到这里。
            return;
        }

        trySpawnWorkers(spawn);

        for (Creep creep : GameObjects.myCreeps()) {
            runWorker(creep, spawn);
        }
    }

    /**
     * 心跳日志：第一 tick 报一次关卡信息，之后每 {@value #REPORT_INTERVAL} tick 报一次态势。
     *
     * <p>真机上"代码没加载成功"和"策略没动作"看起来是一样的——都是场上什么都不发生。
     * 有了这两行，日志面板里能不能看到它们就能把两种情况分开。
     */
    private static void report() {
        int tick = Game.getTicks();
        if (tick == 1) {
            Game.log("[screeps-arena-java] 已加载：" + Game.describeArena());
        }
        if (tick % REPORT_INTERVAL == 0) {
            Game.log("tick=" + tick
                    + " 我方 creep=" + GameObjects.myCreeps().size()
                    + " 能量矿=" + GameObjects.sources().size());
        }
    }

    /**
     * 补 worker。
     *
     * <p>原版 demo 写的是"一只都没有才孵化"。这里换成"少于目标数量就补"，因为 spawn 的能量
     * 是玩家自己搬回来的，一只 worker 的产出太慢。另外必须先问一句 {@code isSpawning()}：
     * 孵化中的 spawn 再调用 {@code spawnCreep} 只会拿到 {@code ERR_BUSY}，白白浪费一次调用。
     */
    private static void trySpawnWorkers(StructureSpawn spawn) {
        if (spawn.isSpawning() || GameObjects.myCreeps().size() >= WANTED_WORKERS) {
            return;
        }
        SpawnCreepResult result = spawn.spawnCreep(WORKER);
        if (!result.isSuccess() && result.error() != ErrorCode.ERR_NOT_ENOUGH_RESOURCES) {
            Game.log("孵化失败：" + result.error());
        }
    }

    /**
     * 一个 tick 的决策：背包没满就去挖，满了就送回家。
     *
     * <p>原版 demo 是"背包里只要有一点能量就往回送"。真游戏里那样会一直耗在路上——
     * 采一个 tick 就往返一趟，几乎攒不下能量。所以这里改成"装满才回"，
     * 判断条件是 {@code getFreeCapacity} 而不是"还剩多少"。
     */
    private static void runWorker(Creep creep, StructureSpawn spawn) {
        Store store = creep.getStore();

        if (store.getFreeCapacity(ResourceType.ENERGY) > 0) {
            harvestSomething(creep);
        } else {
            deliverHome(creep, spawn);
        }
    }

    private static void harvestSomething(Creep creep) {
        // 原版 demo 取的是 sources.get(0)（列表里第一个）。真地图上矿不止一个，
        // 所以换成"最近的那个"，省得绕远路。
        Source source = Game.findClosestByRange(creep, GameObjects.sources());
        if (source == null) {
            return;
        }
        if (creep.rangeTo(source) > 1) {
            creep.moveTo(source);
        } else {
            ErrorCode error = creep.harvest(source);
            if (error != ErrorCode.OK) {
                Game.log("采集失败：" + error);
            }
        }
    }

    private static void deliverHome(Creep creep, StructureSpawn spawn) {
        if (creep.rangeTo(spawn) > 1) {
            creep.moveTo(spawn);
        } else {
            ErrorCode error = creep.transfer(spawn, ResourceType.ENERGY);
            if (error != ErrorCode.OK) {
                Game.log("上交失败：" + error);
            }
        }
    }

    public static void main(String[] args) {
    }
}
