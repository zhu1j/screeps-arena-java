package screeps.arena;

/** 身体部件类型，字符串值与游戏里的 {@code BODYPART_*} 常量一致。 */
public enum BodyPartType {
    MOVE("move"),
    WORK("work"),
    CARRY("carry"),
    ATTACK("attack"),
    RANGED_ATTACK("ranged_attack"),
    TOUGH("tough"),
    HEAL("heal");

    private final String code;

    BodyPartType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static BodyPartType of(String code) {
        for (BodyPartType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
