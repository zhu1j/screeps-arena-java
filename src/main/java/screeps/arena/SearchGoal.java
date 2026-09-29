package screeps.arena;

import java.util.ArrayList;
import java.util.List;

/**
 * 寻路的目标。
 *
 * <p>游戏里的目标是"位置"或者"位置 ± 允许的误差"两种形状。Java 没有联合类型，
 * 所以这里用一个类把两种都表示出来，再用静态工厂区分意图。
 */
public final class SearchGoal {

    private final Position position;
    private final int range;

    private SearchGoal(Position position, int range) {
        this.position = position;
        this.range = range;
    }

    /** 必须走到这个位置本身。 */
    public static SearchGoal at(Position position) {
        return new SearchGoal(position, 0);
    }

    /** 走到距离它 {@code range} 格以内就算到达。 */
    public static SearchGoal within(Position position, int range) {
        if (range < 0) {
            throw new IllegalArgumentException("range 不能是负数：" + range);
        }
        return new SearchGoal(position, range);
    }

    /**
     * 给一批位置套同一个 range。
     *
     * <p>典型用法是逃跑：{@code SearchGoal.withinEach(enemies, 7)} 配
     * {@code new SearchOptions().setFlee(true)}，就是"离每个敌人都至少 7 格"。
     */
    public static List<SearchGoal> withinEach(List<? extends Position> positions, int range) {
        List<SearchGoal> goals = new ArrayList<>(positions.size());
        for (Position position : positions) {
            goals.add(within(position, range));
        }
        return goals;
    }

    public Position getPosition() {
        return position;
    }

    public int getRange() {
        return range;
    }

    @Override
    public String toString() {
        return range == 0 ? position.toString() : position + " 附近 " + range + " 格";
    }
}
