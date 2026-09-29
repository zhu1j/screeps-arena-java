package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 自定义通行代价表。
 *
 * <p>默认代价是 0（表示"用默认值"），设成 255 表示不可通行。
 * 它只在一次 {@link PathFinder#searchPath} 调用里起作用，不需要跨 tick 保存。
 */
public final class CostMatrix {

    private final Raw.CostMatrixHandle raw;

    private CostMatrix(Raw.CostMatrixHandle raw) {
        this.raw = raw;
    }

    /** 新建一个全 0 的代价表。 */
    public static CostMatrix create() {
        return new CostMatrix(Js.newCostMatrix());
    }

    public void set(int x, int y, int cost) {
        raw.set(x, y, cost);
    }

    public int get(int x, int y) {
        return raw.get(x, y);
    }

    /** 复制一份。改副本不会影响原来的表。 */
    public CostMatrix copy() {
        return new CostMatrix((Raw.CostMatrixHandle) raw.cloneMatrix());
    }

    Raw.CostMatrixHandle raw() {
        return raw;
    }
}
