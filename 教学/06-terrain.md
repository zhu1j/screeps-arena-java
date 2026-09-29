# 第 6 关：Terrain（地形）

> 原文：客户端 `src/mocks/tutorial/6-terrain.template.html`（游戏内教程页面）。

游戏里有 5 种**地形**：

- 平原（Plain）
- 天然不可摧毁的墙
- 建造出来的、可摧毁的墙
- 沼泽（Swamp）
- 道路（Road）

沼泽和道路与 **creep 重量**的概念配合生效。除 `MOVE` 外的每个身体部件都会给 creep 增加重量；空的 `CARRY` 部件没有重量，装了资源就有。每个 `MOVE` 部件提高 creep 的移动速度。如果 `MOVE` 的数量和其它部件数量相等，creep 在平原上可以**每 tick 移动**；否则会积累 `fatigue`（疲劳），移动会跳过若干 tick。

而在沼泽地形上、身体部件相同的情况下，它会积累多得多的疲劳，只能每 5 tick 移动一次。想在沼泽上每 tick 移动，`MOVE` 部件要是其它部件的五倍。

道路则相反：它把 creep 需要的 `MOVE` 数量减半。例如 creep 有 10 个有重量的身体部件时，在道路上只要 5 个 `MOVE` 就能每 tick 移动。

本关你要做的就是把每个 creep 走到对应的旗子。注意观察它们因为地形和身体不同而速度各异。

这里可以用 findClosestByPath 函数：当你要用寻路算法在数组里找路径最近的对象时很有用：

```javascript
var flags = getObjectsByPrototype(Flag);
var closestFlag = creep.findClosestByPath(flags);
```

## 本关目标

把所有 creep 移动到各自的旗子上。

## 官方示例代码（JavaScript）

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep, Flag } from 'game/prototypes';

export function loop() {
    var creeps = getObjectsByPrototype(Creep).filter(i => i.my);
    var flags = getObjectsByPrototype(Flag);
    for(var creep of creeps) {
        var flag = creep.findClosestByPath(flags);
        creep.moveTo(flag);
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

```java
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
        for (Creep creep : GameObjects.myCreeps()) {
            Flag flag = Game.findClosestByPath(creep, flags);
            if (flag != null) {
                creep.moveTo(flag);
            }
        }
    }
}
```

- 官方的 `creep.findClosestByPath(flags)` 在绑定库里是工具方法 `Game.findClosestByPath(creep, flags)`——Java 的包装对象上没有这个方法。
- 地形查询用 `Game.getTerrainAt(position)`（返回 `Terrain` 枚举）；距离用 `Game.getRange(a, b)`。
- `getObjectsByPrototype(Creep).filter(i => i.my)` → `GameObjects.myCreeps()`。

[← 第 5 关](05-store-and-transfer.md) | [返回目录](README.md) | [第 7 关 →](07-spawn-creeps.md)
