package screeps.arena;

import org.teavm.jso.JSObject;

/** 墙：没有主人的纯障碍物，是拆墙类策略的目标。 */
public final class StructureWall extends Structure {

    StructureWall(JSObject raw) {
        super(raw);
    }
}
