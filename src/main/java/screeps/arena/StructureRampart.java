package screeps.arena;

import org.teavm.jso.JSObject;

/**
 * 城垛：有主人，但允许友方单位站在上面。
 *
 * <p>耐久上限靠 {@code hitsMax} 反映——升级城垛改的就是这个值。
 */
public final class StructureRampart extends OwnedStructure {

    StructureRampart(JSObject raw) {
        super(raw);
    }
}
