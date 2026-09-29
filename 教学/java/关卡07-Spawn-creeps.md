# 关卡 07：Spawn creeps（孵化 creep）

> 游戏里这一关：孵化两个 creep，让它们分别走到两面旗子上。

## 先学 Java：本关用到的 API

- **跨 tick 状态 = `static` 字段**。第一次赋完值，之后每 tick 都还在这里——
  这就是官方"在模块顶层定义变量"的 Java 写法。**但只用来存数字/开关，不存游戏对象引用**，
  原因见下面"三个真机上的坑"。
- `List.of(BodyPartType.MOVE)` 造一个不可变列表，正好用来描述身体。
  > Java 8 写法：`Arrays.asList(BodyPartType.MOVE)`。
- `spawn.spawnCreep(body)` 返回 `SpawnCreepResult`：`.isSuccess()`、`.creep()`、`.error()`。
- `spawn.isSpawning()` 防御性判断：孵化中再次请求只会白拿一个 `ERR_BUSY`。
- `GameObjects.myCreeps()` / `GameObjects.flags()` 每 tick 现取场上真实情况。
- 孵化费用：MOVE 50、WORK 100、CARRY 50、ATTACK 80、RANGED_ATTACK 150、HEAL 250、TOUGH 10 能量。

## 完整策略（核心逻辑）

```java
import java.util.ArrayList;
import java.util.List;
import org.teavm.jso.JSExport;
import screeps.arena.BodyPartType;
import screeps.arena.Creep;
import screeps.arena.Flag;
import screeps.arena.GameObjects;
import screeps.arena.StructureSpawn;

public final class Main {

    /** 跨 tick 状态：只留纯 Java 的计数器，不存 creep 引用。 */
    private static int ticks;

    @JSExport
    public static void loop() {
        ticks++;
        StructureSpawn spawn = GameObjects.mySpawn();
        List<Flag> flags = GameObjects.flags();
        if (spawn == null || flags.isEmpty()) {
            return;
        }

        // 1) 先移动：每只 creep 认领一面还没主的旗子，认到就立刻走一步
        List<Creep> creeps = GameObjects.myCreeps();
        List<Flag> unclaimed = new ArrayList<>(flags);
        for (Creep creep : creeps) {
            Flag target = nearestFlag(creep, unclaimed);
            if (target != null) {
                creep.moveTo(target);
            }
        }

        // 2) 再补兵：够数、或上一只还在孵化，都不下单
        if (creeps.size() >= flags.size() || spawn.isSpawning()) {
            return;
        }
        spawn.spawnCreep(List.of(BodyPartType.MOVE));   // 返回值可以不看，下一 tick 数场上数量即可
    }

    /** 从还没被认领的旗子里挑一面离 creep 最近的，挑中后划掉，保证两只 creep 不会挤同一面旗。 */
    private static Flag nearestFlag(Creep creep, List<Flag> unclaimed) {
        Flag best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Flag flag : unclaimed) {
            // 切比雪夫距离 = 游戏里 getRange 的定义（横竖斜都算一步），纯 Java 算
            int distance = Math.max(
                    Math.abs(creep.getX() - flag.getX()),
                    Math.abs(creep.getY() - flag.getY()));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = flag;
            }
        }
        if (best != null) {
            unclaimed.remove(best);
        }
        return best;
    }
}
```

> 工程里那份 `level07/Main.java` 在这段核心逻辑外面多包了两件事：
> 两个游戏调用各自 `try/catch`（一个 tick 里异常没接住，剩下的代码就全不执行），
> 以及一行心跳日志（只在 creep 数量变化或每 30 tick 打一次）。

## 再看游戏：这一关要做什么

- 场上一个 Spawn、两面旗子；spawn 出一只就往旗子走一只，两只都到位就通过。
- 官方样例用 `creep.target = flag` 给 creep 挂自定义属性；Java 这边不需要，
  每 tick 用距离重新算一遍"谁去哪面旗"更简单，也不怕对象过期。

## 跑起来

1. 构建 → 绑定目录 → PLAY。
2. 预期：先出现第一只 creep 走向旗子 1，随后第二只走向旗子 2。

## 坑与差异

### 写这一关最容易踩的三个坑

官方示例是"先孵化、后移动"，移动写在 `else` 分支里：

```js
if (!creep1) { creep1 = mySpawn.spawnCreep([MOVE]).object; }
else         { creep1.moveTo(flags[0]); ... }
```

直接照搬成 Java 会踩三个坑：

1. **移动代码可能一个 tick 都跑不到**。只要 `creep1` 没拿到有效的值（还在孵化、或拿到的对象用不了），
   `if (creep1 == null) { ...; return; }` 就一直在挡路。画面表现是"spawn 一直在孵化，creep 站在旁边不动"。
2. **出生点被堵死**。creep 不挪窝，spawn 外围八个落点迟早被占满，之后 `spawnCreep` 只会一直返回
   `ERR_BUSY`，谁也孵不出来——看起来就是"卡住、不生产"。
3. **静态字段里的对象引用会过期**。creep 还没真正出场、或者已经死掉时，字段里那个引用都可能用不了；
   拿它当"孵过了没有"的标志，逻辑就会永远走错分支。

所以这一版：**每 tick 先把场上的 creep 赶向旗子，再决定要不要补兵**，
并且"现在有几只"直接数 `GameObjects.myCreeps()`——数场上的实际情况永远是对的。

### 其它差异

- 比官方多了 `isSpawning()` 判断：官方在孵化中还会调用，白拿一次 `ERR_BUSY`；真实对战里这种防御能省 CPU。
- 孵化返回的 creep 可能在出生的那一 tick 还动不了（正常），下一 tick 就能 `moveTo`。
- 别用实例字段存状态——每 tick 不会重建你的对象，但 `static` 才是和官方"模块级变量"对应的语义。

[← 关卡 06](关卡06-Terrain.md) | [返回目录](README.md) | [关卡 08 →](关卡08-Harvest-energy.md)
