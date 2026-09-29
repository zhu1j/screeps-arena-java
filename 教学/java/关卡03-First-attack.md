# 关卡 03：First attack（第一次攻击）

> 游戏里这一关：消灭敌方 creep。

## 先学 Java：本关用到的 API

- Stream 筛选：`list.stream().findFirst().orElse(null)` —— 找不到时给个 `null`，后面必须判空。
- 本库的快捷方法：`GameObjects.myCreeps()` / `GameObjects.enemyCreeps()`（等价于官方 `filter(creep => creep.my)`）。
- `ErrorCode` 枚举：`creep.attack(target)` 够不着时返回 `ErrorCode.ERR_NOT_IN_RANGE`，这时改成 `moveTo` 追上去。
- 把“打或追”抽成私有方法，`loop()` 保持短小——这是你后面每关都会用的组织方式。

## 完整策略（可直接编译）

```java
import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.ErrorCode;
import screeps.arena.GameObjects;

public final class Main {

    @JSExport
    public static void loop() {
        Creep myCreep = GameObjects.myCreeps().stream().findFirst().orElse(null);
        Creep enemyCreep = GameObjects.enemyCreeps().stream().findFirst().orElse(null);

        if (myCreep == null || enemyCreep == null) {
            return;
        }
        attackOrApproach(myCreep, enemyCreep);
    }

    /** 够得着就打，够不着就朝目标走。 */
    private static void attackOrApproach(Creep creep, Creep target) {
        if (creep.attack(target) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(target);
        }
    }
}
```

## 再看游戏：这一关要做什么

- 场上双方各一个 creep，贴身后每 tick 攻击一次，直到对方被打掉。
- 近战 `attack` 的射程是 1 格；`rangedAttack` 是 3 格（第 04 关会用到）。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：你的 creep 走向敌人并开始攻击，敌人死亡后本关通过。

## 坑与差异

- 官方 JS 用 `.find(...)`；Java 里推荐 Stream 写法 `filter(...).findFirst()`，或者本库快捷方法直接给你筛选好的列表。
- Java 的 `enum` 比较用 `==` 就行（和 JS 数字比较一样直白），不需要 `equals`。
- `findFirst().orElse(null)` 之后一定要判空；这是 Java 和 JS 习惯差异最大的地方。

[← 关卡 02](关卡02-Simple-move.md) | [返回目录](README.md) | [关卡 04 →](关卡04-Creeps-bodies.md)
