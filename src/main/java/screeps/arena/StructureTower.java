package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Raw;

/** 远程防御塔。攻击和治疗的威力都随距离衰减。 */
public final class StructureTower extends OwnedStructure {

    private final Raw.StructureTower raw;

    StructureTower(JSObject raw) {
        super(raw);
        this.raw = (Raw.StructureTower) raw;
    }

    public Store getStore() {
        return new Store(raw.store());
    }

    /** 冷却剩余 tick 数。大于 0 时这一 tick 不能行动。 */
    public int getCooldown() {
        return raw.cooldown();
    }

    public boolean isReady() {
        return raw.cooldown() <= 0;
    }

    public ErrorCode attack(GameObject target) {
        return ErrorCode.of(raw.attack(target.raw()));
    }

    public ErrorCode heal(Creep target) {
        return ErrorCode.of(raw.heal(target.raw()));
    }
}
