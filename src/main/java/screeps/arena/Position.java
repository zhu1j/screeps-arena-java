package screeps.arena;

/**
 * 任何带 {@code x} / {@code y} 的东西。
 *
 * <p>游戏对象和 {@link Point} 都实现它，所以 {@code getRange}、{@code moveTo}、
 * {@code searchPath} 这类接收"位置"的方法可以直接收游戏对象，也可以收一个点。
 */
public interface Position {
    int getX();

    int getY();

    /** 切比雪夫距离，也就是游戏里的 {@code getRange}。 */
    default int rangeTo(Position other) {
        return Math.max(Math.abs(getX() - other.getX()), Math.abs(getY() - other.getY()));
    }
}
