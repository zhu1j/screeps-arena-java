package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Raw;

/** 通用容器，可以存任意资源，容量比 spawn 大得多。 */
public final class StructureContainer extends OwnedStructure {

    private final Raw.StructureContainer raw;

    StructureContainer(JSObject raw) {
        super(raw);
        this.raw = (Raw.StructureContainer) raw;
    }

    public Store getStore() {
        return new Store(raw.store());
    }
}
