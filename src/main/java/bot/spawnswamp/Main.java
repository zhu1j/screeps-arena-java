package bot.spawnswamp;

import java.util.Arrays;
import java.util.List;

import org.teavm.jso.JSExport;

import screeps.arena.BodyPartType;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.Game;
import screeps.arena.GameObject;
import screeps.arena.GameObjects;
import screeps.arena.LineStyle;
import screeps.arena.SpawnCreepResult;
import screeps.arena.StructureSpawn;
import screeps.arena.Visual;

/**
 * Spawn and Swamp 基础关卡的示例 bot。
 *
 * <p>这是用来学 API 的起手式，不是能赢的策略：孵化一队"移动 + 近战"的 creep，
 * 谁离得近就打谁；场上没有敌人时就去拆对面的 spawn。
 *
 * <p>关卡规则以游戏内为准，需要调策略时改 {@link #loop()} 下面的两个方法就行。
 */
public final class Main {

    private Main() {
    }

    /** 战士身体：一个 MOVE 保证能走，一个 ATTACK 保证能打。 */
    private static final List<BodyPartType> FIGHTER =
            Arrays.asList(BodyPartType.MOVE, BodyPartType.ATTACK);

    /** 每隔多少 tick 打一行状态。真机调试时先看它有没有出现。 */
    private static final int REPORT_INTERVAL = 10;

    /** 调试画线用的 Visual 实例；第一次用到时才创建（构造函数会碰游戏模块）。 */
    private static Visual debug;

    @JSExport
    public static void loop() {
        report();

        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null) {
            // 第一 tick 之前游戏对象还没铺开，正常不会走到这里。
            return;
        }
        trySpawn(spawn);

        for (Creep creep : GameObjects.myCreeps()) {
            if (creep.hasBodyPart(BodyPartType.ATTACK)) {
                runFighter(creep);
            }
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
                    + " 敌方 creep=" + GameObjects.enemyCreeps().size());
        }
    }

    /** spawn 一空下来就补一只。能量不够时游戏会返回错误码，等下一 tick 再试即可。 */
    private static void trySpawn(StructureSpawn spawn) {
        if (spawn.isSpawning()) {
            return;
        }
        SpawnCreepResult result = spawn.spawnCreep(FIGHTER);
        if (!result.isSuccess() && result.error() != ErrorCode.ERR_NOT_ENOUGH_RESOURCES) {
            Game.log("孵化失败：" + result.error());
        }
    }

    private static void runFighter(Creep creep) {
        Creep target = nearestEnemy(creep);
        if (target == null) {
            StructureSpawn enemySpawn = GameObjects.enemySpawn();
            if (enemySpawn != null) {
                engage(creep, enemySpawn);
            }
            return;
        }
        engage(creep, target);
    }

    /** 贴身就打，够不着就走过去。顺手画一条线，方便在游戏里看清谁在追谁。 */
    private static void engage(Creep creep, GameObject target) {
        if (debug == null) {
            debug = new Visual();
        }
        debug.line(creep, target, LineStyle.create().setColor("#ff5555"));
        if (creep.rangeTo(target) <= 1) {
            ErrorCode error = creep.attack(target);
            if (error != ErrorCode.OK) {
                Game.log("攻击失败：" + error);
            }
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

    public static void main(String[] args) {
    }
}
