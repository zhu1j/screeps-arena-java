package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 掉在地上的身体部件。
 *
 * <p>进阶 Capture the Flag 里靠搬运它们给 spawn 来孵化 creep；基础关卡里一般见不到。
 */
public final class BodyPart extends GameObject {

    private final Raw.ArenaBodyPart raw;

    BodyPart(JSObject raw) {
        super(raw);
        this.raw = (Raw.ArenaBodyPart) raw;
    }

    public BodyPartType getType() {
        return BodyPartType.of(Js.stringOf(raw.type()));
    }

    public String getTypeCode() {
        return Js.stringOf(raw.type());
    }
}
