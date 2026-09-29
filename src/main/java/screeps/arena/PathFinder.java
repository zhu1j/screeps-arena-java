package screeps.arena;

import java.util.List;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 寻路入口。
 *
 * <p>{@link Creep#moveTo} 用的是引擎自带的寻路，够快但不能定制；这里的 {@code searchPath}
 * 可以指定代价、逃跑模式和多目标，代价是要自己按路径走（见 {@link Game#getDirection}）。
 */
public final class PathFinder {

    private PathFinder() {
    }

    /** 走到某个位置。 */
    public static SearchPathResult searchPath(Position origin, Position goal) {
        return searchPath(origin, SearchGoal.at(goal), new SearchOptions());
    }

    public static SearchPathResult searchPath(Position origin, Position goal, SearchOptions options) {
        return searchPath(origin, SearchGoal.at(goal), options);
    }

    public static SearchPathResult searchPath(Position origin, SearchGoal goal, SearchOptions options) {
        JSObject raw = Js.pathFinder()
                .searchPath(GameObjects.toRaw(origin), rawGoal(goal), options.toRaw());
        return new SearchPathResult((Raw.SearchPathResult) raw);
    }

    /** 多个目标，走到任意一个就算到达。 */
    public static SearchPathResult searchPath(Position origin, List<SearchGoal> goals, SearchOptions options) {
        JSObject array = Js.newArray();
        for (SearchGoal goal : goals) {
            Js.push(array, rawGoal(goal));
        }
        JSObject raw = Js.pathFinder().searchPath(GameObjects.toRaw(origin), array, options.toRaw());
        return new SearchPathResult((Raw.SearchPathResult) raw);
    }

    private static JSObject rawGoal(SearchGoal goal) {
        JSObject position = GameObjects.toRaw(goal.getPosition());
        return goal.getRange() == 0 ? position : Js.goal(position, goal.getRange());
    }
}
