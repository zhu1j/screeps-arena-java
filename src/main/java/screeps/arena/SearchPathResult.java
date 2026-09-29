package screeps.arena;

import java.util.List;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 寻路结果。
 *
 * <p>{@code path} 不包含起点，所以第一步就是 {@code getPath().get(0)}。
 */
public final class SearchPathResult {

    private final Raw.SearchPathResult raw;

    SearchPathResult(Raw.SearchPathResult raw) {
        this.raw = raw;
    }

    public Raw.SearchPathResult raw() {
        return raw;
    }

    /** 从起点下一格开始的路径；没找到路时是空列表。 */
    public List<Point> getPath() {
        return GameObjects.toPoints(Js.orNull(raw.path()));
    }

    public boolean hasPath() {
        return !getPath().isEmpty();
    }

    /** 这次寻路展开了多少个格子，用来估 CPU 开销。 */
    public int getOps() {
        return raw.ops();
    }

    /** 路径的总代价。 */
    public int getCost() {
        return raw.cost();
    }

    /** true 表示搜索被 maxOps 之类限制截断了，结果不是最优甚至可能为空。 */
    public boolean isIncomplete() {
        return raw.incomplete();
    }

    @Override
    public String toString() {
        return "SearchPathResult(" + getPath().size() + " steps, cost " + getCost()
                + (isIncomplete() ? ", incomplete" : "") + ")";
    }
}
