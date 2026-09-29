package screeps.arena;

import screeps.arena.internal.Raw;

/** 孵化进度，来自 spawn.spawning；没有在孵化时该属性不存在。 */
public final class Spawning {

    private final Raw.Spawning raw;

    Spawning(Raw.Spawning raw) {
        this.raw = raw;
    }

    public int getNeedTime() {
        return raw.needTime();
    }

    public int getRemainingTime() {
        return raw.remainingTime();
    }

    public Creep creep() {
        GameObject wrapped = GameObjects.wrap(raw.creep());
        // 原型认不出来时返回 null，别让强转把整个 tick 炸掉。
        return wrapped instanceof Creep ? (Creep) wrapped : null;
    }

    public ErrorCode cancel() {
        return ErrorCode.of(raw.cancel());
    }
}
