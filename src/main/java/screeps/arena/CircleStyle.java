package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;

/** 画圆的样式。{@code fill} 是填充色，{@code stroke} 是描边色。 */
public final class CircleStyle {

    private final JSObject raw = Js.newObject();

    private CircleStyle() {
    }

    public static CircleStyle create() {
        return new CircleStyle();
    }

    /** 半径，默认 0.15。 */
    public CircleStyle setRadius(double radius) {
        Js.setNumber(raw, "radius", radius);
        return this;
    }

    public CircleStyle setFill(String cssColor) {
        Js.setString(raw, "fill", cssColor);
        return this;
    }

    public CircleStyle setStroke(String cssColor) {
        Js.setString(raw, "stroke", cssColor);
        return this;
    }

    public CircleStyle setStrokeWidth(double strokeWidth) {
        Js.setNumber(raw, "strokeWidth", strokeWidth);
        return this;
    }

    public CircleStyle setOpacity(double opacity) {
        Js.setNumber(raw, "opacity", opacity);
        return this;
    }

    public CircleStyle setDashed() {
        Js.setString(raw, "lineStyle", "dashed");
        return this;
    }

    public CircleStyle setDotted() {
        Js.setString(raw, "lineStyle", "dotted");
        return this;
    }

    JSObject raw() {
        return raw;
    }
}
