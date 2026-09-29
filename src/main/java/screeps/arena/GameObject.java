package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 所有游戏对象的基类。
 *
 * <p>包装类持有的是那个<b>活着的</b> JS 对象本身，不是快照。所以 {@link #exists()} 一直反映
 * 最新状态，读取 {@code hits} 这类属性也总是当前值。
 *
 * <p>同一个对象在整场比赛中反复出现时，{@link GameObjects} 会复用同一个包装实例，
 * 因此可以在包装对象上挂自己的字段，并且用 {@code ==} 就能判断是不是同一个游戏对象。
 */
public class GameObject implements Position {

    private final Raw.GameObject raw;

    public GameObject(JSObject raw) {
        this.raw = (Raw.GameObject) raw;
    }

    /** 底层的 JS 对象。逃生舱：绑定层还没覆盖到的属性可以自己从这里取。 */
    public JSObject raw() {
        return raw;
    }

    Raw.GameObject rawObject() {
        return raw;
    }

    /**
     * 游戏内唯一标识，可以用来代替对象引用做持久化。
     *
     * <p>真机的 id 是 {@code number|string}，这里统一转成字符串。孵化中的对象可能还没有 id，
     * 这时返回 {@code null}（这种对象也别拿来当缓存的键）。
     */
    public String getId() {
        return Js.stringOf(raw.id());
    }

    @Override
    public int getX() {
        return raw.x();
    }

    @Override
    public int getY() {
        return raw.y();
    }

    /** 对象是否仍存在于对局中。缓存下来的引用要用它判断有效性。 */
    public boolean exists() {
        return raw.exists();
    }

    /**
     * 还有多少 tick 这个对象会消失；不衰减的对象返回 {@code -1}。
     *
     * <p>官方类型是可选字段：只有地面资源这类对象才有。用 -1 表示"没有"，
     * 避免和"下一 tick 就消失"的 0 混在一起。
     */
    public int getTicksToDecay() {
        JSObject value = raw.ticksToDecay();
        return Js.isNullish(value) ? -1 : Js.numberOrZero(value);
    }

    // ---------------------------------------------------------------- 自定义属性

    /**
     * 给这个游戏对象挂一个自定义属性。
     *
     * <p>竞技场里没有 Memory，但游戏对象整场比赛都活着，所以官方教程推荐的替代方案就是
     * 把状态直接挂在对象上——等价于 Screeps World 的 creep.memory。
     */
    public void setProperty(String name, JSObject value) {
        Js.setObject(raw, name, value);
    }

    /** 之前挂上的自定义属性；没挂过返回 {@code null}。 */
    public JSObject getProperty(String name) {
        return Js.orNull(Js.property(raw, name));
    }

    /** 自定义整数属性；没挂过时返回 0。 */
    public int getIntProperty(String name) {
        return Js.numberPropertyOrZero(raw, name);
    }

    public boolean hasProperty(String name) {
        return !Js.isPropertyUndefined(raw, name);
    }

    /** 记住一个坐标，例如这只 creep 的出生点。 */
    public void setPointProperty(String name, Position position) {
        Js.setObject(raw, name, Js.point(position.getX(), position.getY()));
    }

    /** 之前用 {@link #setPointProperty} 存的坐标；没存过返回 {@code null}。 */
    public Point getPointProperty(String name) {
        JSObject value = getProperty(name);
        if (value == null) {
            return null;
        }
        Raw.Positioned point = (Raw.Positioned) value;
        return Point.of(point.x(), point.y());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getId() + ") at (" + getX() + ", " + getY() + ")";
    }
}
