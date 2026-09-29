# 第 3 关：First attack（第一次攻击）

> 原文：客户端 `src/mocks/tutorial/3-first-attack.template.html`（游戏内教程页面）。

Screeps Arena 是多人 PvP 游戏，你的 creep 会和对手的 creep 战斗，来学学怎么打。

怎么区分哪些 creep 是自己的？用 JavaScript 标准的 [Array.filter](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter) 或 [Array.find](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find) 方法，按条件筛选数组元素。在这里，条件就是 Creep 的 `my` 属性：

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep } from 'game/prototypes';

export function loop() {
    var myCreep = getObjectsByPrototype(Creep).find(creep => creep.my);
    var enemyCreep = getObjectsByPrototype(Creep).find(creep => !creep.my);
}
```

拿到敌人后，用 `attack` 方法攻击它。如果距离太远，它会返回 `ERR_NOT_IN_RANGE`，所以要先调用 `moveTo` 靠近：

```javascript
if(myCreep.attack(enemyCreep) == ERR_NOT_IN_RANGE) {
    myCreep.moveTo(enemyCreep);
}
```

## 本关目标

消灭敌方 creep。

## 官方示例代码（JavaScript）

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep } from 'game/prototypes';
import { ERR_NOT_IN_RANGE } from 'game/constants';

export function loop() {
    var myCreep = getObjectsByPrototype(Creep).find(creep => creep.my);
    var enemyCreep = getObjectsByPrototype(Creep).find(creep => !creep.my);
    if(myCreep.attack(enemyCreep) == ERR_NOT_IN_RANGE) {
        myCreep.moveTo(enemyCreep);
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

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
        if (myCreep.attack(enemyCreep) == ErrorCode.ERR_NOT_IN_RANGE) {
            myCreep.moveTo(enemyCreep);
        }
    }
}
```

- `creep.my` → `creep.isMine()`；筛选推荐用 Stream：`Game.getObjectsByPrototype(Creep.class).stream().filter(Creep::isMine).findFirst()`。
- `ERR_NOT_IN_RANGE` → `ErrorCode.ERR_NOT_IN_RANGE`。
- 空值检查是 Java 和 JS 最明显的习惯差异之一：`findFirst().orElse(null)` 之后要先判空再调用方法。

[← 第 2 关](02-simple-move.md) | [返回目录](README.md) | [第 4 关 →](04-creeps-bodies.md)
