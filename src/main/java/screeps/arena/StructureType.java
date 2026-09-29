package screeps.arena;

/**
 * 能放置工地的建筑种类，对应 {@code /game/constants} 里的 {@code BuildableStructure}。
 *
 * <p>游戏里的 {@code createConstructionSite} 收的是"原型构造函数"（TypeScript 里写成
 * {@code StructureTower} 这种标识符）。Java 没有这种值，所以用枚举代替，
 * 由 {@link GameObjects} 负责翻译成对应的原型对象。
 */
public enum StructureType {
    TOWER("tower"),
    EXTENSION("extension"),
    ROAD("road"),
    CONTAINER("container"),
    WALL("constructedWall"),
    RAMPART("rampart"),
    SPAWN("spawn");

    private final String code;

    StructureType(String code) {
        this.code = code;
    }

    /** {@code /game/constants} 里的 {@code STRUCTURE_*} 字符串。 */
    public String code() {
        return code;
    }
}
