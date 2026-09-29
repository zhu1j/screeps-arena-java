package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;

/**
 * 画线的样式。
 *
 * <p>配色用网页格式（{@code "#ff0000"} 这种）。不设的字段由游戏用默认值补。
 */
public final class LineStyle {

    private final JSObject raw = Js.newObject();

    private LineStyle() {
    }

    public static LineStyle create() {
        return new LineStyle();
    }

    public LineStyle setColor(String cssColor) {
        Js.setString(raw, "color", cssColor);
        return this;
    }

    public LineStyle setWidth(double width) {
        Js.setNumber(raw, "width", width);
        return this;
    }

    public LineStyle setOpacity(double opacity) {
        Js.setNumber(raw, "opacity", opacity);
        return this;
    }

    public LineStyle setDashed() {
        Js.setString(raw, "lineStyle", "dashed");
        return this;
    }

    public LineStyle setDotted() {
        Js.setString(raw, "lineStyle", "dotted");
        return this;
    }

    JSObject raw() {
        return raw;
    }
}
