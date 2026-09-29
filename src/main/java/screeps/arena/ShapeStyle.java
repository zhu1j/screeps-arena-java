package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;

/**
 * 折线和矩形的样式。
 *
 * <p>官方给 poly 和 rect 的样式字段完全一样（fill/stroke/strokeWidth/opacity/lineStyle），
 * 所以合成一个类，不为两个同形结构各写一份。
 */
public final class ShapeStyle {

    private final JSObject raw = Js.newObject();

    private ShapeStyle() {
    }

    public static ShapeStyle create() {
        return new ShapeStyle();
    }

    /** 填充色，网页格式（{@code "#ff0000"}）。 */
    public ShapeStyle setFill(String cssColor) {
        Js.setString(raw, "fill", cssColor);
        return this;
    }

    /** 描边色，网页格式。 */
    public ShapeStyle setStroke(String cssColor) {
        Js.setString(raw, "stroke", cssColor);
        return this;
    }

    public ShapeStyle setStrokeWidth(double strokeWidth) {
        Js.setNumber(raw, "strokeWidth", strokeWidth);
        return this;
    }

    public ShapeStyle setOpacity(double opacity) {
        Js.setNumber(raw, "opacity", opacity);
        return this;
    }

    public ShapeStyle setDashed() {
        Js.setString(raw, "lineStyle", "dashed");
        return this;
    }

    public ShapeStyle setDotted() {
        Js.setString(raw, "lineStyle", "dotted");
        return this;
    }

    JSObject raw() {
        return raw;
    }
}
