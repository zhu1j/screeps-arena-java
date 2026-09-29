package screeps.arena;

/** 八个方向，数值与游戏里的 {@code DirectionConstant} 一致。 */
public enum Direction {
    TOP(1),
    TOP_RIGHT(2),
    RIGHT(3),
    BOTTOM_RIGHT(4),
    BOTTOM(5),
    BOTTOM_LEFT(6),
    LEFT(7),
    TOP_LEFT(8);

    private final int code;

    Direction(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    /** 认不出的方向码返回 {@code null}，例如 {@code getDirection(0, 0)} 的结果。 */
    public static Direction of(int code) {
        for (Direction direction : values()) {
            if (direction.code == code) {
                return direction;
            }
        }
        return null;
    }
}
