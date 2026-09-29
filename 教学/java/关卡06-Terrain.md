# 关卡 06：Terrain（地形）

> 游戏里这一关：把所有 creep 走到各自的旗子上，顺便体会地形对速度的影响。

## 先学 Java：本关用到的 API

- 泛型方法：`Game.findClosestByPath(Position, List<T>)` 的返回类型跟着列表走，
  传 `List<Flag>` 就得到 `Flag`——Java 的泛型帮你省掉强制转换。
- `for (Creep creep : list)`（增强 for 循环）遍历列表。
- 位置就是对象：`GameObject` 实现了 `Position`，`findClosestByPath` 直接收 creep 自己。
- 距离：`creep.rangeTo(flag)` 或 `Game.getRange(a, b)`（切比雪夫距离）。

## 完整策略（可直接编译）

```java
import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSExport;
import screeps.arena.Creep;
import screeps.arena.Flag;
import screeps.arena.Game;
import screeps.arena.GameObjects;

public final class Main {

    @JSExport
    public static void loop() {
        List<Flag> flags = GameObjects.flags();
        if (flags.isEmpty()) {
            return;
        }

        // 认领：被挑走的旗子从候选里划掉，保证每只 creep 各分到一面旗（先到先得）。
        List<Flag> unclaimed = new ArrayList<>(flags);
        for (Creep creep : GameObjects.myCreeps()) {
            Flag flag = Game.findClosestByPath(creep, unclaimed);
            if (flag == null) {
                continue;
            }
            unclaimed.remove(flag);
            creep.moveTo(flag);
        }
    }
}
```

## 再看游戏：这一关要做什么

- 每个 creep 都有一面自己的旗子；用“路径最近”而不是“直线最近”来找，绕开墙更聪明。
- 注意“每只 creep 各到自己的旗子”：官方示例直接每只都挑最近的，两只 creep 可能同时挑中同一面旗、
  挤在终点。所以本次实现加了一步“认领”——挑走的旗子从候选里划掉，后面的 creep 只能挑剩下的。
- 地形知识：平原正常走；沼泽让没有足够 `MOVE` 的 creep 变得极慢；道路相反，要求减半；
  天然墙不可摧毁、建造的墙可以拆。想每 tick 都移动，`MOVE` 数量要够。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：每个 creep 各自走向最近的旗子；全到位后本关通过。

## 坑与差异

- 官方把 `findClosestByPath` 写在 creep 对象上（`creep.findClosestByPath(flags)`）；
  本库是静态方法 `Game.findClosestByPath(creep, flags)`——原因是 Java 的包装类不往对象上挂业务方法。
- `findClosestByPath` 可能返回 `null`（列表为空），一定要判空。
- `unclaimed.remove(flag)` 能生效的前提是两次取到的是同一个 Java 对象——绑定库按对象 id 缓存包装实例，
  所以从列表里删得掉；换成自己 new 出来的副本就删不掉。

[← 关卡 05](关卡05-Store-and-transfer.md) | [返回目录](README.md) | [关卡 07 →](关卡07-Spawn-creeps.md)
