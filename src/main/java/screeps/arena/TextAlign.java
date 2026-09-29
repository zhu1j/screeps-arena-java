package screeps.arena;

/** 文本相对于锚点的对齐方式。 */
public enum TextAlign {
    CENTER("center"),
    LEFT("left"),
    RIGHT("right");

    private final String code;

    TextAlign(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
