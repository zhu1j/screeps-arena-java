# 关卡 02：Simple move（简单移动）

> 游戏里这一关：让你的 creep 走到旗子上。

## 先学 Java：本关用到的 API

- `Game.getObjectsByPrototype(Creep.class)` → `List<Creep>`；`Flag.class` 同理。
- `List.get(0)` 取第一个元素；先判 `isEmpty()`，别直接下标访问。
- `List`、`String` 这些是 JDK 自带类，TeaVM 支持其中的常用子集。
- `creep.moveTo(position)` 返回 `ErrorCode`；示例忽略返回值，进阶时再检查。

## 完整策略（可直接编译）

```java
import java.util.List;
import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.Flag;
import screeps.arena.Game;

public final class Main {

    @JSExport
    public static void loop() {
        List<Creep> creeps = Game.getObjectsByPrototype(Creep.class);
        List<Flag> flags = Game.getObjectsByPrototype(Flag.class);

        if (creeps.isEmpty() || flags.isEmpty()) {
            return;
        }
        creeps.get(0).moveTo(flags.get(0));
    }
}
```

## 再看游戏：这一关要做什么

- 场上有一个 creep 和一面 Flag；`moveTo` 要每 tick 调用一次，creep 才会一直走，直到踩上旗子。
- creep 的位置是 `loop` 结束后才更新的——这是游戏的“先登记、后执行”机制，Java 侧行为一样。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：creep 一步步走向旗子，到达后本关通过。

## 坑与差异

- 官方示例取的是“所有 creep 的第 0 个”。实战关卡里敌人也是 creep，记得换成本库的
  `GameObjects.myCreeps()`（内部按 `isMine()` 过滤），别把自己的敌人当自己人。
- Java 的列表下标越界会抛异常（不是返回 undefined）；拿不到东西时先判空再操作。

[← 关卡 01](关卡01-Loop-and-import.md) | [返回目录](README.md) | [关卡 03 →](关卡03-First-attack.md)
