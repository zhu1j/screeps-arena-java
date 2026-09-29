package screeps.arena;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 当前对局的元信息，对应 {@code /game} 模块里的 {@code arenaInfo}。
 *
 * <p>这些值在整场比赛里不变，但第一 tick 之前读不到，所以不要在静态初始化块里取。
 */
public final class ArenaInfo {

    private final Raw.ArenaInfo raw;

    ArenaInfo(Raw.ArenaInfo raw) {
        this.raw = raw;
    }

    public Raw.ArenaInfo raw() {
        return raw;
    }

    /** 关卡名，例如 "Capture the Flag"、"Spawn and Swamp"、"Collect and Control"。 */
    public String getName() {
        return Js.stringOf(raw.name());
    }

    /** 1 是基础关卡，2 是进阶关卡。 */
    public int getLevel() {
        return raw.level();
    }

    public String getSeason() {
        return Js.stringOf(raw.season());
    }

    /** 这一局最多打多少 tick。 */
    public int getTicksLimit() {
        return raw.ticksLimit();
    }

    /** 之后每 tick 的墙钟时间上限，单位是秒。 */
    public double getCpuTimeLimit() {
        return raw.cpuTimeLimit();
    }

    /** 第一 tick 的墙钟时间上限。游戏对象还没铺开，但加载脚本的开销也算在里面。 */
    public double getCpuTimeLimitFirstTick() {
        return raw.cpuTimeLimitFirstTick();
    }

    @Override
    public String toString() {
        return "ArenaInfo(" + getName() + " level " + getLevel() + ")";
    }
}
