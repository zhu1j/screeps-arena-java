package screeps.arena;

import java.util.List;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 调试可视化。画的图形只影响画面，不影响对局。
 *
 * <p>官方 {@code /game/visual} 导出的<b>是一个类</b>，不是一组模块级函数：
 * 先 {@code new Visual()} 建实例，再在实例上调用画图方法。每个画图方法返回实例本身，
 * 所以可以链式调用：
 *
 * <pre>
 * private static Visual debug;          // 跨 tick 用同一个实例
 * ...
 * if (debug == null) {
 *     debug = new Visual();
 * }
 * debug.clear().line(creep, target, LineStyle.create().setColor("#ff5555"));
 * </pre>
 *
 * <p>非持久的图形每 tick 会被清掉，所以要在 {@code loop()} 里每 tick 重画；
 * 图层（layer）越大越靠前。
 */
public final class Visual {

    private final Raw.Visual raw;

    /** 新建一层可视化：图层 0、非持久（每 tick 自动清掉）。 */
    public Visual() {
        this.raw = Js.newVisual();
    }

    /** 新建一层可视化并指定图层；图层越大越靠前。 */
    public Visual(double layer) {
        this.raw = Js.newVisual(layer);
    }

    /**
     * 新建一层可视化。
     *
     * @param layer      图层，越大越靠前
     * @param persistent 为 true 时图形跨 tick 保留，需要自己记得 {@link #clear()}
     */
    public Visual(double layer, boolean persistent) {
        this.raw = Js.newVisual(layer, persistent);
    }

    /** 当前图层。 */
    public double getLayer() {
        return raw.layer();
    }

    /** 图形是否跨 tick 保留。 */
    public boolean isPersistent() {
        return raw.persistent();
    }

    /** 画一条线。 */
    public Visual line(Position from, Position to) {
        return line(from, to, LineStyle.create());
    }

    public Visual line(Position from, Position to, LineStyle style) {
        raw.line(GameObjects.toRaw(from), GameObjects.toRaw(to), style.raw());
        return this;
    }

    /** 在某个位置画一个圆。 */
    public Visual circle(Position center) {
        return circle(center, CircleStyle.create());
    }

    public Visual circle(Position center, CircleStyle style) {
        raw.circle(GameObjects.toRaw(center), style.raw());
        return this;
    }

    /** 在某个位置写一段文字。 */
    public Visual text(String text, Position position) {
        return text(text, position, TextStyle.create());
    }

    public Visual text(String text, Position position, TextStyle style) {
        raw.text(text, GameObjects.toRaw(position), style.raw());
        return this;
    }

    /** 画一条折线。 */
    public Visual poly(List<Position> points) {
        return poly(points, ShapeStyle.create());
    }

    public Visual poly(List<Position> points, ShapeStyle style) {
        JSObject array = Js.newArray();
        for (Position point : points) {
            Js.push(array, GameObjects.toRaw(point));
        }
        raw.poly(array, style.raw());
        return this;
    }

    /** 以 {@code topLeft} 为左上角画一个矩形。 */
    public Visual rect(Position topLeft, double width, double height) {
        return rect(topLeft, width, height, ShapeStyle.create());
    }

    public Visual rect(Position topLeft, double width, double height, ShapeStyle style) {
        raw.rect(GameObjects.toRaw(topLeft), width, height, style.raw());
        return this;
    }

    /** 清掉这个实例画过的全部图形。 */
    public Visual clear() {
        raw.clear();
        return this;
    }

    /** 这个实例当前存了多少字节的图形数据。 */
    public int size() {
        return raw.size();
    }
}
