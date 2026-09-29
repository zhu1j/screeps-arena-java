package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Raw;

/** 能量扩展，只用来给 spawn 增加可用的孵化能量。 */
public final class StructureExtension extends OwnedStructure {

    private final Raw.StructureExtension raw;

    StructureExtension(JSObject raw) {
        super(raw);
        this.raw = (Raw.StructureExtension) raw;
    }

    public Store getStore() {
        return new Store(raw.store());
    }
}
