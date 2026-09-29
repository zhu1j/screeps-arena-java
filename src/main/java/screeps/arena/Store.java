package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/** 结构或 creep 身上的资源容器。 */
public final class Store {

    private final Raw.Store raw;

    Store(JSObject raw) {
        this.raw = (Raw.Store) raw;
    }

    Raw.Store raw() {
        return raw;
    }

    /** 当前存有该资源的数量。 */
    public int amount(ResourceType resource) {
        return Js.numberPropertyOrZero(raw, resource.code());
    }

    /** 这个 store 的总容量（所有资源加在一起）。 */
    public int getCapacity() {
        return Js.numberOrZero(raw.getCapacity());
    }

    /** 该资源的容量上限；资源类型对这件容器无效时返回 0。 */
    public int getCapacity(ResourceType resource) {
        return Js.numberOrZero(raw.getCapacity(resource.code()));
    }

    /** 已经用掉的容量总和。 */
    public int getUsedCapacity() {
        return Js.numberOrZero(raw.getUsedCapacity());
    }

    /** 指定资源已经用掉的容量。 */
    public int getUsedCapacity(ResourceType resource) {
        return Js.numberOrZero(raw.getUsedCapacity(resource.code()));
    }

    /** 还能装多少。 */
    public int getFreeCapacity() {
        return Js.numberOrZero(raw.getFreeCapacity());
    }

    /** 指定资源还能装多少。 */
    public int getFreeCapacity(ResourceType resource) {
        return Js.numberOrZero(raw.getFreeCapacity(resource.code()));
    }

    @Override
    public String toString() {
        return "Store(energy=" + amount(ResourceType.ENERGY) + ")";
    }
}
