package screeps.arena;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/** 能移动、采集、建造和战斗的单位。 */
public final class Creep extends GameObject {

    private final Raw.Creep raw;

    Creep(JSObject raw) {
        super(raw);
        this.raw = (Raw.Creep) raw;
    }

    public boolean isMine() {
        return raw.my();
    }

    public int getHits() {
        return raw.hits();
    }

    public int getHitsMax() {
        return raw.hitsMax();
    }

    /** 疲劳度。大于 0 时这一 tick 无法移动。 */
    public int getFatigue() {
        return raw.fatigue();
    }

    /** 身体部件列表，顺序与孵化时给的一致。 */
    public List<BodyPartStatus> getBody() {
        JSObject body = raw.body();
        int size = Js.length(body);
        List<BodyPartStatus> parts = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            parts.add(new BodyPartStatus((Raw.BodyPartEntry) Js.elementAt(body, index)));
        }
        return parts;
    }

    /** 是否还有活着的这类部件。 */
    public boolean hasBodyPart(BodyPartType type) {
        for (BodyPartStatus part : getBody()) {
            if (part.isActive() && part.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public Store getStore() {
        return new Store(raw.store());
    }

    /** 是否还在孵化中（还没真正出场）。孵化中的 creep 也还没有 id。 */
    public boolean isSpawning() {
        return raw.spawning();
    }

    /** 往指定方向走一格。 */
    public ErrorCode move(Direction direction) {
        return ErrorCode.of(raw.move(direction.code()));
    }

    /** 朝目标寻路并移动。目标可以是游戏对象，也可以是裸坐标。 */
    public ErrorCode moveTo(Position target) {
        return ErrorCode.of(raw.moveTo(GameObjects.toRaw(target)));
    }

    /** 近身攻击，目标必须紧邻。 */
    public ErrorCode attack(GameObject target) {
        return ErrorCode.of(raw.attack(target.raw()));
    }

    /** 远程攻击，目标需在 3 格内。 */
    public ErrorCode rangedAttack(GameObject target) {
        return ErrorCode.of(raw.rangedAttack(target.raw()));
    }

    /** 对 3 格内所有敌对目标同时远程攻击，不伤及友军。 */
    public ErrorCode rangedMassAttack() {
        return ErrorCode.of(raw.rangedMassAttack());
    }

    /** 治疗相邻目标。 */
    public ErrorCode heal(Creep target) {
        return ErrorCode.of(raw.heal(target.raw()));
    }

    /** 远程治疗 3 格内的目标，效果弱于 heal。 */
    public ErrorCode rangedHeal(Creep target) {
        return ErrorCode.of(raw.rangedHeal(target.raw()));
    }

    public ErrorCode harvest(Source target) {
        return ErrorCode.of(raw.harvest(target.raw()));
    }

    /** 把身上全部该资源转移给目标。 */
    public ErrorCode transfer(GameObject target, ResourceType resource) {
        return ErrorCode.of(raw.transfer(target.raw(), resource.code()));
    }

    public ErrorCode transfer(GameObject target, ResourceType resource, int amount) {
        return ErrorCode.of(raw.transfer(target.raw(), resource.code(), amount));
    }

    /** 从目标身上取出全部该资源。 */
    public ErrorCode withdraw(GameObject target, ResourceType resource) {
        return ErrorCode.of(raw.withdraw(target.raw(), resource.code()));
    }

    public ErrorCode withdraw(GameObject target, ResourceType resource, int amount) {
        return ErrorCode.of(raw.withdraw(target.raw(), resource.code(), amount));
    }

    /** 把资源丢在地上。 */
    public ErrorCode drop(ResourceType resource) {
        return ErrorCode.of(raw.drop(resource.code()));
    }

    public ErrorCode drop(ResourceType resource, int amount) {
        return ErrorCode.of(raw.drop(resource.code(), amount));
    }

    /** 捡起同格或相邻格的地面资源。 */
    public ErrorCode pickup(Resource target) {
        return ErrorCode.of(raw.pickup(target.raw()));
    }

    /** 拖着另一只 creep 一起走。目标必须相邻，否则返回 {@code ERR_NOT_IN_RANGE}。 */
    public ErrorCode pull(Creep target) {
        return ErrorCode.of(raw.pull(target.raw()));
    }

    /** 给工地搬运能量，目标需在 3 格内。 */
    public ErrorCode build(ConstructionSite target) {
        return ErrorCode.of(raw.build(target.raw()));
    }
}
