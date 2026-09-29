package screeps.arena;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;

/** 画文字的样式。 */
public final class TextStyle {

    private final JSObject raw = Js.newObject();

    private TextStyle() {
    }

    public static TextStyle create() {
        return new TextStyle();
    }

    public TextStyle setColor(String cssColor) {
        Js.setString(raw, "color", cssColor);
        return this;
    }

    /** 字号，单位是格，例如 0.7 表示比一格略小。 */
    public TextStyle setFontSize(double size) {
        Js.setNumber(raw, "font", size);
        return this;
    }

    /** 用 CSS 字体写法指定字体，例如 {@code "bold italic 1.5 Times New Roman"}。 */
    public TextStyle setFont(String cssFont) {
        Js.setString(raw, "font", cssFont);
        return this;
    }

    public TextStyle setAlign(TextAlign align) {
        Js.setString(raw, "align", align.code());
        return this;
    }

    public TextStyle setBackgroundColor(String cssColor) {
        Js.setString(raw, "backgroundColor", cssColor);
        return this;
    }

    /** 背景框的内边距，单位是格。 */
    public TextStyle setBackgroundPadding(double padding) {
        Js.setNumber(raw, "backgroundPadding", padding);
        return this;
    }

    /** 文字描边色；默认没有描边。 */
    public TextStyle setStroke(String cssColor) {
        Js.setString(raw, "stroke", cssColor);
        return this;
    }

    public TextStyle setStrokeWidth(double strokeWidth) {
        Js.setNumber(raw, "strokeWidth", strokeWidth);
        return this;
    }

    public TextStyle setOpacity(double opacity) {
        Js.setNumber(raw, "opacity", opacity);
        return this;
    }

    JSObject raw() {
        return raw;
    }
}
