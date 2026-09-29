package screeps.arena;

/** 孵化的结果：成功时带对象，失败时带返回码。 */
public final class SpawnCreepResult {

    private final Creep creep;
    private final ErrorCode error;

    SpawnCreepResult(Creep creep, ErrorCode error) {
        this.creep = creep;
        this.error = error;
    }

    public boolean isSuccess() {
        return creep != null;
    }

    /** 正在孵化的 creep；失败时为 null。 */
    public Creep creep() {
        return creep;
    }

    /** 成功时为 ErrorCode.OK。 */
    public ErrorCode error() {
        return error;
    }

    @Override
    public String toString() {
        return isSuccess() ? "SpawnCreepResult(ok, " + creep + ")" : "SpawnCreepResult(" + error + ")";
    }
}
