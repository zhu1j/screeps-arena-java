package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/** 掉在地上的一堆资源，要用 {@link Creep#pickup(Resource)} 捡。 */
public final class Resource extends GameObject {

    private final Raw.Resource raw;

    Resource(JSObject raw) {
        super(raw);
        this.raw = (Raw.Resource) raw;
    }

    public int getAmount() {
        return raw.amount();
    }

    /**
     * 资源类型。v0.1 只认 {@link ResourceType#ENERGY}，遇到还没绑定的资源返回 {@code null}，
     * 拿不准时用 {@link #getResourceTypeCode()} 看原始字符串。
     */
    public ResourceType getResourceType() {
        return ResourceType.of(Js.stringOf(raw.resourceType()));
    }

    public String getResourceTypeCode() {
        return Js.stringOf(raw.resourceType());
    }
}
