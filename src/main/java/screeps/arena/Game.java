package screeps.arena;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 全局函数的门面：tick 数、CPU、地形、查找、放工地。
 *
 * <p>竞技场里没有 Screeps World 那种全局 {@code Game} 对象，所有全局能力都要从
 * {@code /game/utils} 之类的模块里 import。这个类把这些函数收成一组静态方法，
 * 顺便把返回的裸 JS 对象换成 Java 包装对象。
 *
 * <p>静态状态（{@code static} 字段）是跨 tick 存活的，见 {@code iterations/README.md}。
 */
public final class Game {

    private Game() {
    }

    // ---------------------------------------------------------------- 时间

    /** 从开局算起经过了多少 tick。第一 tick 返回 1。 */
    public static int getTicks() {
        return Js.utils().getTicks();
    }

    /** 当前 tick 已经用掉的墙钟时间，单位纳秒。 */
    public static double getCpuTime() {
        return Js.utils().getCpuTime();
    }

    public static double getCpuTimeMillis() {
        return Js.utils().getCpuTime() / 1_000_000.0;
    }

    /**
     * 当前对局的元信息；这个关卡没导出 {@code arenaInfo} 时返回 {@code null}。
     *
     * <p>{@code arenaInfo} 挂在 {@code /game} 根模块上，第一 tick 之前读不到，也不保证每个
     * 关卡都有。所以别在静态初始化块里取，拿到之后也要判空——直接取名字会让整个 tick 抛异常。
     */
    public static ArenaInfo getArenaInfo() {
        Raw.ArenaInfo raw = Js.arenaInfo();
        return Js.isNullish(raw) ? null : new ArenaInfo(raw);
    }

    /**
     * 一行关卡描述，给日志用，例如 {@code "Capture the Flag（最多 500 tick）"}。
     *
     * <p>关卡没导出 {@code arenaInfo} 时返回一句说明，而不是让调用方把 {@code null} 拼进日志——
     * "代码没加载成功"和"这个关卡没这个字段"在日志里要能分开。
     */
    public static String describeArena() {
        ArenaInfo info = getArenaInfo();
        if (info == null) {
            return "未知关卡（没有 arenaInfo）";
        }
        return info.getName() + "（最多 " + info.getTicksLimit() + " tick）";
    }

    /** 往游戏控制台打一行日志。调试全靠它。 */
    public static void log(String message) {
        Js.consoleLog(message);
    }

    // ---------------------------------------------------------------- 地形与距离

    public static Terrain getTerrainAt(Position position) {
        return Terrain.of(Js.numberOrZero(Js.utils().getTerrainAt(GameObjects.toRaw(position))));
    }

    /** 两个位置之间的切比雪夫距离，也就是游戏里的 {@code getRange}。 */
    public static int getRange(Position from, Position to) {
        return Js.utils().getRange(GameObjects.toRaw(from), GameObjects.toRaw(to));
    }

    /**
     * 从位移量求方向。
     *
     * <p>只用符号判断，所以 {@code (3, -7)} 和 {@code (1, -1)} 都是 TOP_RIGHT。
     * 两个分量都是 0 时返回 {@code null}——"原地不动"不是一个方向。
     */
    public static Direction getDirection(int dx, int dy) {
        int x = sign(dx);
        int y = sign(dy);
        if (x == 0 && y == 0) {
            return null;
        }
        if (y == 0) {
            return x < 0 ? Direction.LEFT : Direction.RIGHT;
        }
        if (x == 0) {
            return y < 0 ? Direction.TOP : Direction.BOTTOM;
        }
        if (y < 0) {
            return x < 0 ? Direction.TOP_LEFT : Direction.TOP_RIGHT;
        }
        return x < 0 ? Direction.BOTTOM_LEFT : Direction.BOTTOM_RIGHT;
    }

    /** 从 {@code from} 看向 {@code to} 的方向。 */
    public static Direction getDirection(Position from, Position to) {
        return getDirection(to.getX() - from.getX(), to.getY() - from.getY());
    }

    private static int sign(int value) {
        if (value < 0) {
            return -1;
        }
        return value > 0 ? 1 : 0;
    }

    // ---------------------------------------------------------------- 查找

    /** 对局里当前存在的全部对象。顺序不保证。 */
    public static List<GameObject> getObjects() {
        return GameObjects.all();
    }

    /** 取回某种原型的全部对象，例如 {@code Game.getObjectsByPrototype(Creep.class)}。 */
    public static <T extends GameObject> List<T> getObjectsByPrototype(Class<T> type) {
        return GameObjects.of(type);
    }

    /** 按 id 取对象；对象已经不在了返回 {@code null}。 */
    public static GameObject getObjectById(String id) {
        return GameObjects.byId(id);
    }

    /** 这些候选目标里，距离 {@code from} 不超过 {@code range} 的那些。 */
    public static <T extends GameObject> List<T> findInRange(Position from, List<T> candidates, int range) {
        JSObject found = Js.orNull(
                Js.utils().findInRange(GameObjects.toRaw(from), toRawArray(candidates), range));
        return wrapAll(found);
    }

    /** 最近的目标；候选为空时返回 {@code null}。 */
    public static <T extends GameObject> T findClosestByRange(Position from, List<T> candidates) {
        JSObject closest = Js.orNull(
                Js.utils().findClosestByRange(GameObjects.toRaw(from), toRawArray(candidates)));
        return closest == null ? null : wrap(closest);
    }

    /** 按实际路径长度算最近的目标；候选为空时返回 {@code null}。 */
    public static <T extends GameObject> T findClosestByPath(Position from, List<T> candidates) {
        JSObject closest = Js.orNull(
                Js.utils().findClosestByPath(GameObjects.toRaw(from), toRawArray(candidates)));
        return closest == null ? null : wrap(closest);
    }

    // ---------------------------------------------------------------- 建造

    /**
     * 在某处放置工地。
     *
     * <p>和孵化一样是"二选一"的返回：成功带工地对象，失败带返回码。
     */
    public static ConstructionSiteResult createConstructionSite(Position position, StructureType type) {
        Raw.ConstructionSiteResult result = (Raw.ConstructionSiteResult) Js.utils()
                .createConstructionSite(GameObjects.toRaw(position), GameObjects.prototypeFor(type));
        JSObject object = Js.orNull(result.object());
        if (object == null) {
            return new ConstructionSiteResult(null, ErrorCode.of(Js.numberOrZero(result.error())));
        }
        GameObject wrapped = GameObjects.wrap(object);
        if (!(wrapped instanceof ConstructionSite)) {
            return new ConstructionSiteResult(null, ErrorCode.OK);
        }
        return new ConstructionSiteResult((ConstructionSite) wrapped, ErrorCode.OK);
    }

    // ---------------------------------------------------------------- 内部

    private static JSObject toRawArray(List<? extends GameObject> objects) {
        JSObject array = Js.newArray();
        for (GameObject object : objects) {
            Js.push(array, object.raw());
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static <T extends GameObject> List<T> wrapAll(JSObject array) {
        List<T> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        int size = Js.length(array);
        for (int index = 0; index < size; index++) {
            result.add((T) GameObjects.wrap(Js.elementAt(array, index)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends GameObject> T wrap(JSObject raw) {
        return (T) GameObjects.wrap(raw);
    }
}
