package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 基础 Capture the Flag 关卡里的旗子。
 *
 * <p>旗子分三种：自己的、敌人的、中立的。中立旗子身上没有 {@code my} 属性，
 * 所以 {@link #isNeutral()} 才是"既不属于我也不属于敌人"的准确判断。
 */
public final class Flag extends GameObject {

    private final Raw.Flag raw;

    Flag(JSObject raw) {
        super(raw);
        this.raw = (Raw.Flag) raw;
    }

    /** 中立的旗子返回 false。 */
    public boolean isMine() {
        return Js.booleanProperty(raw, "my");
    }

    public boolean isEnemy() {
        return !isNeutral() && !isMine();
    }

    public boolean isNeutral() {
        return Js.isPropertyUndefined(raw, "my");
    }
}
