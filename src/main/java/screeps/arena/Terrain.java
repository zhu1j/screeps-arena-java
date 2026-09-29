package screeps.arena;

/** 地形类型，数值与游戏里的 {@code TERRAIN_*} 一致。 */
public enum Terrain {
    PLAIN(0),
    WALL(1),
    SWAMP(2);

    private final int code;

    Terrain(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static Terrain of(int code) {
        for (Terrain terrain : values()) {
            if (terrain.code == code) {
                return terrain;
            }
        }
        return PLAIN;
    }

    public boolean isWalkable() {
        return this != WALL;
    }
}
