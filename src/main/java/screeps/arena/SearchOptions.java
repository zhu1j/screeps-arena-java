package screeps.arena;

import java.util.List;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;

/**
 * 寻路参数。所有字段都是可选的，不设就用游戏自己的默认值。
 *
 * <p>setter 返回 {@code this}，所以可以链着写：
 * <pre>
 * new SearchOptions().setSwampCost(10).setMaxOps(2000)
 * </pre>
 */
public final class SearchOptions {

    private CostMatrix costMatrix;
    private Integer plainCost;
    private Integer swampCost;
    private Boolean flee;
    private Integer maxOps;
    private Integer maxCost;
    private Double heuristicWeight;
    private List<Position> ignore;

    /** 自定义的通行代价表，用来绕开或强行穿过某些格子。 */
    public SearchOptions setCostMatrix(CostMatrix costMatrix) {
        this.costMatrix = costMatrix;
        return this;
    }

    /** 走平地的代价，默认 2。 */
    public SearchOptions setPlainCost(int plainCost) {
        this.plainCost = plainCost;
        return this;
    }

    /** 走沼泽的代价，默认 10。 */
    public SearchOptions setSwampCost(int swampCost) {
        this.swampCost = swampCost;
        return this;
    }

    /** true 表示"远离目标"而不是"走向目标"。 */
    public SearchOptions setFlee(boolean flee) {
        this.flee = flee;
        return this;
    }

    /** 最多允许展开多少个格子，默认 50000。调小可以给 CPU 让路。 */
    public SearchOptions setMaxOps(int maxOps) {
        this.maxOps = maxOps;
        return this;
    }

    /** 路径总代价的上限。 */
    public SearchOptions setMaxCost(int maxCost) {
        this.maxCost = maxCost;
        return this;
    }

    /** 启发式权重，1 到 9，默认 1.2。越大越快但路径可能不是最短。 */
    public SearchOptions setHeuristicWeight(double heuristicWeight) {
        this.heuristicWeight = heuristicWeight;
        return this;
    }

    /** 寻路时假装不存在的障碍。 */
    public SearchOptions setIgnore(List<Position> ignore) {
        this.ignore = ignore;
        return this;
    }

    /** 翻译成游戏 API 要的 JS 对象，只带上真正设过的字段。 */
    JSObject toRaw() {
        JSObject raw = Js.newObject();
        if (costMatrix != null) {
            Js.setObject(raw, "costMatrix", costMatrix.raw());
        }
        if (plainCost != null) {
            Js.setNumber(raw, "plainCost", plainCost);
        }
        if (swampCost != null) {
            Js.setNumber(raw, "swampCost", swampCost);
        }
        if (flee != null) {
            Js.setBoolean(raw, "flee", flee);
        }
        if (maxOps != null) {
            Js.setNumber(raw, "maxOps", maxOps);
        }
        if (maxCost != null) {
            Js.setNumber(raw, "maxCost", maxCost);
        }
        if (heuristicWeight != null) {
            Js.setNumber(raw, "heuristicWeight", heuristicWeight);
        }
        if (ignore != null && !ignore.isEmpty()) {
            JSObject array = Js.newArray();
            for (Position position : ignore) {
                Js.push(array, GameObjects.toRaw(position));
            }
            Js.setObject(raw, "ignore", array);
        }
        return raw;
    }
}
