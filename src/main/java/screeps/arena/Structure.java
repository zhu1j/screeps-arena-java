package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Raw;

/**
 * 所有建筑的基类。
 *
 * <p>游戏里 {@code Structure} 和 {@code OwnedStructure} 是两层原型：墙和路没有主人，
 * 只继承第一层；spawn、tower 这些有主人的建筑继承第二层。这里如实照搬这个层次，
 * 于是"能不能对某个建筑调用 {@code isMine()}"就是编译期就能确定的事。
 */
public class Structure extends GameObject {

    private final Raw.Structure raw;

    Structure(JSObject raw) {
        super(raw);
        this.raw = (Raw.Structure) raw;
    }

    public int getHits() {
        return raw.hits();
    }

    public int getHitsMax() {
        return raw.hitsMax();
    }

    /** 是否受过伤。维修逻辑用它筛目标。 */
    public boolean isDamaged() {
        return raw.hits() < raw.hitsMax();
    }
}
