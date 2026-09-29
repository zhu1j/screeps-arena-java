package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Raw;

/** 有归属的建筑。{@link #isMine()} 为 false 时就是可以攻击的目标。 */
public class OwnedStructure extends Structure {

    private final Raw.OwnedStructure raw;

    OwnedStructure(JSObject raw) {
        super(raw);
        this.raw = (Raw.OwnedStructure) raw;
    }

    public boolean isMine() {
        return raw.my();
    }
}
