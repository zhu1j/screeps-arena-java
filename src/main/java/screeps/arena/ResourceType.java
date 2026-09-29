package screeps.arena;

/**
 * 资源类型。
 *
 * <p>v0.1 只覆盖基础关卡会用到的 {@link #ENERGY}。进阶赛季（Collect &amp; Control）还会出现
 * {@code score_x} 这类资源，届时在这里补枚举值即可，见 {@code iterations/ideas/003}。
 */
public enum ResourceType {
    ENERGY("energy");

    private final String code;

    ResourceType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ResourceType of(String code) {
        for (ResourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
