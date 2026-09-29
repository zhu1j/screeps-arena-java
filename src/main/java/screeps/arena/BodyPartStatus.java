package screeps.arena;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/** creep 身上某个身体部件的状态，来自 creep.body。 */
public final class BodyPartStatus {

    private final Raw.BodyPartEntry raw;

    BodyPartStatus(Raw.BodyPartEntry raw) {
        this.raw = raw;
    }

    public BodyPartType getType() {
        return BodyPartType.of(Js.stringOf(raw.type()));
    }

    /** 剩余耐久。归零的部件不再提供能力。 */
    public int getHits() {
        return raw.hits();
    }

    public boolean isActive() {
        return raw.hits() > 0;
    }

    @Override
    public String toString() {
        return getType() + "(" + getHits() + ")";
    }
}
