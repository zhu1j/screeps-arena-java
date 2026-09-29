package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/** 还没建完的建筑。creep 搬运能量过来，进度满了就会变成真正的 {@link Structure}。 */
public final class ConstructionSite extends GameObject {

    private final Raw.ConstructionSite raw;

    ConstructionSite(JSObject raw) {
        super(raw);
        this.raw = (Raw.ConstructionSite) raw;
    }

    /** 已经投入的建造进度。 */
    public int getProgress() {
        return raw.progress();
    }

    /** 建成需要的总进度。 */
    public int getProgressTotal() {
        return raw.progressTotal();
    }

    public int getRemainingProgress() {
        return raw.progressTotal() - raw.progress();
    }

    /**
     * 建成后是什么建筑，取值来自 {@code /game/constants} 里的 {@code STRUCTURE_*}。
     * 真机上是 {@code "StructureTower"} 这种带前缀的名字；读不到（字段缺失）时返回 {@code null}。
     */
    public String getStructurePrototypeName() {
        return Js.stringOf(raw.structurePrototypeName());
    }

    public boolean isMine() {
        return raw.my();
    }

    /** 撤销这个工地。已经投入的能量不会退回。 */
    public void remove() {
        raw.remove();
    }
}
