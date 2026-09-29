# 关卡 04：Creeps bodies（Creep 的身体部件）

> 游戏里这一关：用 ATTACK / RANGED_ATTACK / HEAL 三种 creep 配合作战，击败敌人。

## 先学 Java：本关用到的 API

- `creep.hasBodyPart(BodyPartType.ATTACK)` —— 判断身体部件，比官方 `body.some(...)` 更直接。
  （想要底层数据可以 `creep.getBody()` 拿 `List<BodyPartStatus>`，里面有 `getType()`、`getHits()`、`isActive()`。）
- `BodyPartType` 是枚举：`ATTACK`、`RANGED_ATTACK`、`HEAL`、`MOVE`、`WORK`、`CARRY`、`TOUGH`。
- `if / else if` 链给每种 creep 分配职责——这是“按能力分工”的雏形，之后所有策略都长这样。
- `creep.heal(...)` 的参数类型是 `Creep`（不能治疗建筑），`attack` / `rangedAttack` 收任意 `GameObject`。

## 完整策略（可直接编译）

```java
import org.teavm.jso.JSExport;
import screeps.arena.BodyPartType;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.GameObjects;

public final class Main {

    @JSExport
    public static void loop() {
        Creep enemy = GameObjects.enemyCreeps().stream().findFirst().orElse(null);
        if (enemy == null) {
            return;
        }
        for (Creep creep : GameObjects.myCreeps()) {
            if (creep.hasBodyPart(BodyPartType.ATTACK)) {
                attackOrApproach(creep, enemy);
            } else if (creep.hasBodyPart(BodyPartType.RANGED_ATTACK)) {
                rangedAttackOrApproach(creep, enemy);
            } else if (creep.hasBodyPart(BodyPartType.HEAL)) {
                healWounded(creep);
            }
        }
    }

    /** 近战：够得着就打，够不着就追。 */
    private static void attackOrApproach(Creep creep, Creep target) {
        if (creep.attack(target) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(target);
        }
    }

    /** 远程：3 格内就能打。 */
    private static void rangedAttackOrApproach(Creep creep, Creep target) {
        if (creep.rangedAttack(target) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(target);
        }
    }

    /** 治疗：找第一个受伤的队友处理。 */
    private static void healWounded(Creep healer) {
        Creep wounded = GameObjects.myCreeps().stream()
                .filter(c -> c.getHits() < c.getHitsMax())
                .findFirst()
                .orElse(null);
        if (wounded == null) {
            return;
        }
        if (healer.heal(wounded) == ErrorCode.ERR_NOT_IN_RANGE) {
            healer.moveTo(wounded);
        }
    }
}
```

## 再看游戏：这一关要做什么

- 你有三种 creep，敌方一个 creep；让近战贴脸、远程在 3 格内输出、治疗给受伤队友回血，就能拿下。
- 每个身体部件 100 点 `hits`；受伤过重会失去能力——`heal` 要优先给挨打的人。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：三种 creep 各司其职，敌人被击杀后本关通过。

## 坑与差异

- `getHits() < getHitsMax()` 这种比较在本库返回的都是 `int`，但**在 JS 里是属性访问**——写 Java 时别漏括号。
- `heal()` 只能接收 `Creep`；如果你写 `heal(GameObject)` 编译不过，这是刻意的类型约束。

[← 关卡 03](关卡03-First-attack.md) | [返回目录](README.md) | [关卡 05 →](关卡05-Store-and-transfer.md)
