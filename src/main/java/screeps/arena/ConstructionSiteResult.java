package screeps.arena;

/**
 * 放置工地的结果：成功时带工地对象，失败时带返回码。
 *
 * <p>形状和 {@link SpawnCreepResult} 一样，同样是"二选一"的返回约定。
 */
public final class ConstructionSiteResult {

    private final ConstructionSite site;
    private final ErrorCode error;

    ConstructionSiteResult(ConstructionSite site, ErrorCode error) {
        this.site = site;
        this.error = error;
    }

    public boolean isSuccess() {
        return site != null;
    }

    /** 新建的工地；失败时为 null。 */
    public ConstructionSite site() {
        return site;
    }

    /** 成功时为 ErrorCode.OK。 */
    public ErrorCode error() {
        return error;
    }

    @Override
    public String toString() {
        return isSuccess() ? "ConstructionSiteResult(ok, " + site + ")" : "ConstructionSiteResult(" + error + ")";
    }
}
