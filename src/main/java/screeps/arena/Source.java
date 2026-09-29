package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Raw;

/** 能量矿。creep 用 {@link Creep#harvest(Source)} 采集。 */
public final class Source extends GameObject {

    private final Raw.Source raw;

    Source(JSObject raw) {
        super(raw);
        this.raw = (Raw.Source) raw;
    }

    public int getEnergy() {
        return raw.energy();
    }

    public int getEnergyCapacity() {
        return raw.energyCapacity();
    }

    public boolean isEmpty() {
        return raw.energy() <= 0;
    }
}
