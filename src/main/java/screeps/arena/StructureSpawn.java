package screeps.arena;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/** 孵化 creep 的建筑，通常也是整局比赛里你唯一一开始就拥有的东西。 */
public final class StructureSpawn extends OwnedStructure {

    private final Raw.StructureSpawn raw;

    StructureSpawn(JSObject raw) {
        super(raw);
        this.raw = (Raw.StructureSpawn) raw;
    }

    public Store getStore() {
        return new Store(raw.store());
    }

    /**
     * 正在孵化的那个 creep，没有孵化时返回 {@code null}。
     *
     * <p>游戏文档说这里返回 null，实际返回的是 {@code undefined}，所以判断"能不能孵化"
     * 必须用它，而不是看 {@code spawning} 是不是 null。
     */
    public Spawning getSpawning() {
        JSObject spawning = raw.spawning();
        return Js.isNullish(spawning) ? null : new Spawning((Raw.Spawning) spawning);
    }

    public boolean isSpawning() {
        return getSpawning() != null;
    }

    /**
     * 新 creep 出场时允许移动的方向；没有设置过时返回空列表。
     *
     * <p>方向和 {@link #setDirections(List)} 对应。没设过时游戏有自己的默认规则，
     * 这里读到的就是游戏给的当前值。
     */
    public List<Direction> getDirections() {
        List<Direction> result = new ArrayList<>();
        JSObject directions = raw.directions();
        if (Js.isNullish(directions)) {
            return result;
        }
        int size = Js.length(directions);
        for (int index = 0; index < size; index++) {
            Direction direction = Direction.of(Js.numberOrZero(Js.elementAt(directions, index)));
            if (direction != null) {
                result.add(direction);
            }
        }
        return result;
    }

    /**
     * 开始孵化一个 creep。
     *
     * <p>返回结果里成功带 {@code creep}，失败带返回码，两者不会同时出现——调用方得自己挑，
     * 所以这里包成了 {@link SpawnCreepResult} 而不是抛异常：孵化失败在游戏里是常态
     * （能量不够、正在孵化），不是程序错误。
     *
     * <p>能量是从 spawn 和所有 extension 里一起扣的，所以"够不够"没法只看 spawn 自己的
     * {@code store}——照常调用，读返回码里有没有 {@link ErrorCode#ERR_NOT_ENOUGH_RESOURCES} 即可。
     */
    public SpawnCreepResult spawnCreep(List<BodyPartType> body) {
        // 自己拼一个真正的 JS 数组。TeaVM 的数组转换是"按声明类型"来的：String[] 能转对，
        // int[] 之类会转出一串 undefined，所以传数组给游戏这一步不指望它的自动转换。
        JSObject codes = Js.newArray();
        for (int index = 0; index < body.size(); index++) {
            Js.setElement(codes, index, body.get(index).code());
        }
        Raw.SpawnCreepResult result = (Raw.SpawnCreepResult) raw.spawnCreep(codes);
        JSObject object = Js.orNull(result.object());
        if (object == null) {
            return new SpawnCreepResult(null, ErrorCode.of(Js.numberOrZero(result.error())));
        }
        GameObject wrapped = GameObjects.wrap(object);
        if (!(wrapped instanceof Creep)) {
            // 原型没认出来（例如关卡专属 creep）就当这次没拿到对象，好过在强转处炸掉整个 tick。
            return new SpawnCreepResult(null, ErrorCode.OK);
        }
        return new SpawnCreepResult((Creep) wrapped, ErrorCode.OK);
    }

    /** 指定新孵化的 creep 一出来就往哪几个方向移动，用来让它立刻让开 spawn 的位置。 */
    public ErrorCode setDirections(List<Direction> directions) {
        JSObject codes = Js.newArray();
        for (int index = 0; index < directions.size(); index++) {
            Js.setNumberElement(codes, index, directions.get(index).code());
        }
        return ErrorCode.of(raw.setDirections(codes));
    }
}
