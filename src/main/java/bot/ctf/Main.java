package bot.ctf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.teavm.jso.JSExport;

import screeps.arena.BodyPartType;
import screeps.arena.Creep;
import screeps.arena.Direction;
import screeps.arena.ErrorCode;
import screeps.arena.Flag;
import screeps.arena.Game;
import screeps.arena.GameObjects;
import screeps.arena.PathFinder;
import screeps.arena.Point;
import screeps.arena.SearchGoal;
import screeps.arena.SearchOptions;
import screeps.arena.SearchPathResult;
import screeps.arena.SpawnCreepResult;
import screeps.arena.StructureSpawn;

/**
 * 基础 Capture the Flag 关卡的示例 bot。
 *
 * <p>结构和官方 TypeScript 示例一致：按身体部件给 creep 分工——
 * 近战守家、远程推进、治疗跟着救人。旗帜相关的判断集中在 {@link #targetFlag()}。
 *
 * <p>跨 tick 的状态（这里只有"下一种身体该孵什么"）用静态字段保存，
 * 因为竞技场里没有 Memory，而模块的静态字段整场比赛都活着。
 */
public final class Main {

    private Main() {
    }

    private static final List<BodyPartType> MELEE =
            Arrays.asList(BodyPartType.MOVE, BodyPartType.ATTACK);
    private static final List<BodyPartType> RANGED =
            Arrays.asList(BodyPartType.MOVE, BodyPartType.RANGED_ATTACK);
    private static final List<BodyPartType> HEALER =
            Arrays.asList(BodyPartType.MOVE, BodyPartType.HEAL);

    /** 三种身体轮流孵，保证队伍里近战、远程、治疗都有。 */
    private static final List<List<BodyPartType>> ROLES = Arrays.asList(MELEE, RANGED, HEALER);

    /** 近战只在出生点附近这么多格内应战，太远就回家。 */
    private static final int DEFEND_RANGE = 10;

    private static final String INITIAL_POS = "initialPos";

    /** 已经安排出去的第几套身体。跨 tick 保留。 */
    private static int nextRole;

    /** 每隔多少 tick 打一行状态。真机调试时先看它有没有出现。 */
    private static final int REPORT_INTERVAL = 10;

    @JSExport
    public static void loop() {
        report();

        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null) {
            return;
        }
        trySpawn(spawn);

        List<Creep> myCreeps = GameObjects.myCreeps();
        List<Creep> enemies = GameObjects.enemyCreeps();

        for (Creep creep : myCreeps) {
            if (creep.hasBodyPart(BodyPartType.HEAL)) {
                runHealer(creep, myCreeps, enemies);
            } else if (creep.hasBodyPart(BodyPartType.RANGED_ATTACK)) {
                runRanged(creep, enemies);
            } else if (creep.hasBodyPart(BodyPartType.ATTACK)) {
                runMelee(creep, enemies);
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
                    + " 敌方 creep=" + GameObjects.enemyCreeps().size()
                    + " 旗子=" + GameObjects.flags().size());
        }
    }

    private static void trySpawn(StructureSpawn spawn) {
        if (spawn.isSpawning()) {
            return;
        }
        List<BodyPartType> body = ROLES.get(nextRole % ROLES.size());
        SpawnCreepResult result = spawn.spawnCreep(body);
        if (result.isSuccess()) {
            nextRole++;
        } else if (result.error() != ErrorCode.ERR_NOT_ENOUGH_RESOURCES) {
            Game.log("孵化失败：" + result.error());
        }
    }

    // ---------------------------------------------------------------- 近战

    /**
     * 近战守家：只打靠近我方出生点的敌人，没有敌人就回到出生点待命。
     *
     * <p>出生点是第一次行动时记在 creep 对象上的自定义属性——竞技场没有 Memory，
     * 状态要么放静态字段，要么就挂在游戏对象上。
     */
    private static void runMelee(Creep creep, List<Creep> enemies) {
        Point home = creep.getPointProperty(INITIAL_POS);
        if (home == null) {
            home = Point.of(creep.getX(), creep.getY());
            creep.setPointProperty(INITIAL_POS, home);
        }

        Creep target = nearestWithin(creep, enemies, home, DEFEND_RANGE);
        if (target == null) {
            if (creep.rangeTo(home) > 0) {
                creep.moveTo(home);
            }
            return;
        }
        if (creep.rangeTo(target) <= 1) {
            creep.attack(target);
        } else {
            creep.moveTo(target);
        }
    }

    // ---------------------------------------------------------------- 远程

    private static void runRanged(Creep creep, List<Creep> enemies) {
        Creep target = nearest(creep, enemies);
        if (target != null && creep.rangeTo(target) <= 3) {
            creep.rangedAttack(target);
        }

        List<Creep> threats = closerThan(creep, enemies, 3);
        if (!threats.isEmpty()) {
            flee(creep, threats, 3);
            return;
        }

        Flag flag = targetFlag();
        if (flag != null) {
            creep.moveTo(flag);
        }
    }

    // ---------------------------------------------------------------- 治疗

    private static void runHealer(Creep creep, List<Creep> myCreeps, List<Creep> enemies) {
        Creep wounded = null;
        int lowestHits = Integer.MAX_VALUE;
        for (Creep other : myCreeps) {
            if (other != creep && other.getHits() < other.getHitsMax() && other.getHits() < lowestHits) {
                lowestHits = other.getHits();
                wounded = other;
            }
        }

        if (wounded != null) {
            int range = creep.rangeTo(wounded);
            if (range <= 1) {
                creep.heal(wounded);
            } else if (range <= 3) {
                creep.rangedHeal(wounded);
            } else {
                creep.moveTo(wounded);
            }
        } else {
            Flag flag = targetFlag();
            if (flag != null) {
                creep.moveTo(flag);
            }
        }

        List<Creep> threats = closerThan(creep, enemies, 7);
        if (!threats.isEmpty()) {
            flee(creep, threats, 7);
        }
    }

    // ---------------------------------------------------------------- 工具

    /**
     * 逃跑：让寻路器找一条"离所有威胁都至少 range 格"的路。
     *
     * <p>注意路径里的坐标是不含起点的，所以直接看第一步。
     */
    private static void flee(Creep creep, List<Creep> threats, int range) {
        SearchPathResult result = PathFinder.searchPath(
                creep,
                SearchGoal.withinEach(threats, range),
                new SearchOptions().setFlee(true));
        List<Point> path = result.getPath();
        if (path.isEmpty()) {
            return;
        }
        Direction direction = Game.getDirection(creep, path.get(0));
        if (direction != null) {
            creep.move(direction);
        }
    }

    private static Creep nearest(Creep creep, List<Creep> candidates) {
        Creep nearest = null;
        int bestRange = Integer.MAX_VALUE;
        for (Creep candidate : candidates) {
            int range = creep.rangeTo(candidate);
            if (range < bestRange) {
                bestRange = range;
                nearest = candidate;
            }
        }
        return nearest;
    }

    /** 离 {@code anchor} 不超过 {@code maxRange} 格、且离 creep 最近的那个。 */
    private static Creep nearestWithin(Creep creep, List<Creep> candidates, Point anchor, int maxRange) {
        Creep nearest = null;
        int bestRange = Integer.MAX_VALUE;
        for (Creep candidate : candidates) {
            if (candidate.rangeTo(anchor) > maxRange) {
                continue;
            }
            int range = creep.rangeTo(candidate);
            if (range < bestRange) {
                bestRange = range;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static List<Creep> closerThan(Creep creep, List<Creep> candidates, int range) {
        List<Creep> close = new ArrayList<>();
        for (Creep candidate : candidates) {
            if (creep.rangeTo(candidate) < range) {
                close.add(candidate);
            }
        }
        return close;
    }

    /**
     * 该去抢的旗子：敌人的优先，没有就选中立的。
     *
     * <p>基础 CTF 的实际胜负判定以游戏内为准，这里只是"往不属于我们的旗子推进"。
     */
    private static Flag targetFlag() {
        Flag neutral = null;
        for (Flag flag : GameObjects.flags()) {
            if (flag.isEnemy()) {
                return flag;
            }
            if (neutral == null && flag.isNeutral()) {
                neutral = flag;
            }
        }
        return neutral;
    }

    public static void main(String[] args) {
    }
}
