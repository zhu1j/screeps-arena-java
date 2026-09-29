package bot.probe;

import java.util.Arrays;
import java.util.List;

import org.teavm.jso.JSExport;

import screeps.arena.BodyPartStatus;
import screeps.arena.BodyPartType;
import screeps.arena.CircleStyle;
import screeps.arena.ConstructionSite;
import screeps.arena.Creep;
import screeps.arena.Direction;
import screeps.arena.ErrorCode;
import screeps.arena.Flag;
import screeps.arena.Game;
import screeps.arena.GameObject;
import screeps.arena.GameObjects;
import screeps.arena.LineStyle;
import screeps.arena.Point;
import screeps.arena.Resource;
import screeps.arena.ShapeStyle;
import screeps.arena.SpawnCreepResult;
import screeps.arena.Spawning;
import screeps.arena.Store;
import screeps.arena.StructureSpawn;
import screeps.arena.TextStyle;
import screeps.arena.Visual;

/**
 * 绑定库自检 bot：不是给人玩的，而是把"关卡用不到、出问题却很难查"的角落跑一遍。
 *
 * <p>每一行结果都以 {@code probe: } 开头，离线测试 {@code tools/tests/probe.mjs} 照着断言。
 * 目前覆盖：
 *
 * <ul>
 *   <li>{@code getId()} / {@code Game.getObjectById()}——真机 id 是 {@code number|string}，
 *       用字符串形式的数字 id 查一次、再让绑定层自己兜底；</li>
 *   <li>孵化中的 creep 没有 id 时还能不能用、同一个对象是不是同一个 Java 实例；</li>
 *   <li>孵化完成后，那个"还没有 id"的包装实例会被同一个对象认领（不能变成两个实例）；</li>
 *   <li>工地的 {@code structurePrototypeName}、{@code creep.body} 的部件类型、掉落资源的类型；</li>
 *   <li>旗子的"我的 / 中立 / 敌方"三种状态；</li>
 *   <li>{@code /game/visual} 的类形状（新建实例 + 链式画图）、{@code Store} 的无参容量查询、
 *       {@code GameObject.ticksToDecay}、spawn 的 {@code directions}、creep 的
 *       {@code spawning} 与 {@code pull}。</li>
 * </ul>
 */
public final class Main {

    private Main() {
    }

    private static int tick;

    /** 存活到孵化的那一刻，用来检查跨 tick 后拿到的还是不是同一个实例。 */
    private static Creep spawning;

    @JSExport
    public static void loop() {
        tick++;
        if (tick == 1) {
            checkLookup();
            checkSpawnResult();
            checkConstructionSites();
            checkBodyParts();
            checkResources();
            checkFlags();
            checkVisual();
            checkDirections();
            checkStoreTotals();
            checkTicksToDecay();
            checkPull();
            return;
        }
        if (spawning != null && spawning.exists()) {
            // 孵化中的 creep 不算入场：真机要等 spawning 翻成 false 才进 Game.creeps。
            // 所以判"认领"必须等出壳那一刻，不能一看到 exists 就查。
            if (!spawning.isSpawning()) {
                boolean adopted = GameObjects.myCreeps().contains(spawning);
                Game.log("probe: adopted=" + (adopted ? "yes" : "no") + " id=" + spawning.getId());
                spawning = null;
            }
        }
    }

    private static void checkLookup() {
        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null) {
            Game.log("probe: no spawn");
            return;
        }
        Game.log("probe: spawn id=" + spawn.getId());
        GameObject found = Game.getObjectById(spawn.getId());
        Game.log("probe: byId=" + (found == spawn ? "same" : found == null ? "null" : "other"));
        Game.log("probe: byId missing=" + (Game.getObjectById("no-such-id") == null ? "null" : "other"));
    }

    private static void checkSpawnResult() {
        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null || spawn.isSpawning()) {
            return;
        }
        SpawnCreepResult result = spawn.spawnCreep(Arrays.asList(BodyPartType.MOVE));
        Game.log("probe: spawnCreep ok=" + result.isSuccess());
        spawning = result.creep();
        if (spawning == null) {
            return;
        }
        // 真机上孵化中的 creep 还没有 id，这里期望是 null。
        Game.log("probe: spawning id=" + spawning.getId());
        Game.log("probe: spawning flag=" + spawning.isSpawning());
        Spawning status = spawn.getSpawning();
        if (status != null) {
            Game.log("probe: spawning instance=" + (status.creep() == spawning ? "same" : "other"));
        }
    }

    private static void checkConstructionSites() {
        List<ConstructionSite> sites = GameObjects.of(ConstructionSite.class);
        Game.log("probe: sites=" + sites.size());
        for (ConstructionSite site : sites) {
            Game.log("probe: prototype=" + site.getStructurePrototypeName());
        }
    }

    private static void checkBodyParts() {
        List<Creep> creeps = GameObjects.myCreeps();
        if (creeps.isEmpty()) {
            Game.log("probe: no creeps");
            return;
        }
        List<BodyPartStatus> body = creeps.get(0).getBody();
        Game.log("probe: creep spawning=" + creeps.get(0).isSpawning());
        Game.log("probe: body size=" + body.size());
        if (!body.isEmpty()) {
            Game.log("probe: bodypart=" + body.get(0).getType() + " hits=" + body.get(0).getHits());
        }
    }

    private static void checkResources() {
        List<Resource> resources = GameObjects.of(Resource.class);
        Game.log("probe: resources=" + resources.size());
        for (Resource resource : resources) {
            Game.log("probe: resource=" + resource.getResourceTypeCode() + " amount=" + resource.getAmount());
        }
    }

    private static void checkFlags() {
        int mine = 0;
        int neutral = 0;
        int enemy = 0;
        for (Flag flag : GameObjects.flags()) {
            if (flag.isNeutral()) {
                neutral++;
            } else if (flag.isMine()) {
                mine++;
            } else {
                enemy++;
            }
        }
        Game.log("probe: flags mine=" + mine + " neutral=" + neutral + " enemy=" + enemy);
    }

    /**
     * {@code /game/visual} 导出的 Visual 是类，不是模块级函数：new 一个实例，
     * 把 line/circle/text/poly/rect/clear 整条链走一遍。形状不对会直接抛异常。
     */
    private static void checkVisual() {
        Visual visual = new Visual(5, false);
        visual.clear()
                .line(Point.of(1, 1), Point.of(2, 2), LineStyle.create().setColor("#ff0000"))
                .circle(Point.of(3, 3), CircleStyle.create().setRadius(0.3))
                .text("probe", Point.of(4, 4), TextStyle.create().setFontSize(0.5))
                .poly(Arrays.asList(Point.of(1, 1), Point.of(2, 2), Point.of(3, 1)),
                        ShapeStyle.create().setFill("#00ff00"))
                .rect(Point.of(5, 5), 2, 1, ShapeStyle.create().setStroke("#0000ff"));
        Game.log("probe: visual layer=" + (int) visual.getLayer() + " size=" + visual.size());
    }

    /** spawn 的 directions：先读默认值，再设置、再读回来。 */
    private static void checkDirections() {
        StructureSpawn spawn = GameObjects.mySpawn();
        if (spawn == null) {
            return;
        }
        Game.log("probe: directions before=" + spawn.getDirections().size());
        spawn.setDirections(Arrays.asList(Direction.TOP, Direction.RIGHT));
        Game.log("probe: directions after=" + spawn.getDirections().size());
    }

    /** Store 的无参容量查询：总数、已用、剩余。 */
    private static void checkStoreTotals() {
        List<Creep> creeps = GameObjects.myCreeps();
        if (creeps.isEmpty()) {
            return;
        }
        Store store = creeps.get(0).getStore();
        Game.log("probe: store capacity=" + store.getCapacity()
                + " free=" + store.getFreeCapacity()
                + " used=" + store.getUsedCapacity());
    }

    /** 没有该字段的对象应该返回 -1，而不是 0。 */
    private static void checkTicksToDecay() {
        List<Resource> resources = GameObjects.of(Resource.class);
        if (resources.isEmpty()) {
            return;
        }
        Game.log("probe: decay=" + resources.get(0).getTicksToDecay());
    }

    /** 拖动相邻的另一只 creep。 */
    private static void checkPull() {
        List<Creep> creeps = GameObjects.myCreeps();
        if (creeps.size() < 2) {
            Game.log("probe: pull=no-second-creep");
            return;
        }
        ErrorCode error = creeps.get(0).pull(creeps.get(1));
        Game.log("probe: pull=" + error);
    }
}
