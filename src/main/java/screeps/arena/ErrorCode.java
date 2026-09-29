package screeps.arena;

/** 游戏方法的返回值码。 */
public enum ErrorCode {
    OK(0),
    ERR_NOT_OWNER(-1),
    ERR_NO_PATH(-2),
    ERR_NAME_EXISTS(-3),
    ERR_BUSY(-4),
    ERR_NOT_FOUND(-5),
    ERR_NOT_ENOUGH_RESOURCES(-6),
    ERR_INVALID_TARGET(-7),
    ERR_FULL(-8),
    ERR_NOT_IN_RANGE(-9),
    ERR_INVALID_ARGS(-10),
    ERR_TIRED(-11),
    ERR_NO_BODYPART(-12),
    /** 竞技场返回了预期之外的数值，或者字段缺席。 */
    UNKNOWN(Integer.MIN_VALUE);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public boolean isOk() {
        return this == OK;
    }

    public static ErrorCode of(int code) {
        for (ErrorCode candidate : values()) {
            if (candidate.code == code) {
                return candidate;
            }
        }
        return UNKNOWN;
    }

    /** 把可能缺席的返回值映射成返回码；{@code null} 视为 {@link #UNKNOWN}。 */
    public static ErrorCode ofNullable(int code, boolean present) {
        return present ? of(code) : UNKNOWN;
    }
}
