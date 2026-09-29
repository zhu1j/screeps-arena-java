# 第 2 关：Simple move（简单移动）

> 原文：客户端 `src/mocks/tutorial/2-simple-move.template.html`（游戏内教程页面）。

干得漂亮！记住：每个步骤的默认脚本目录都不一样，所以这一关要去另一个目录改文件。当然，目录随时可以改。

现在来认识你的单位——creep。文档里可以看到，每个 creep 都能做各种动作，当然也能移动：

```javascript
creep.moveTo(target);
```

这条命令让 creep 朝目标走一步。如果每次 `loop` 都执行，它就会一直走到目的地。注意：调用这一行时它的位置**不会立刻改变**——位置要等 `loop` 函数结束后才更新。

怎么从代码里拿到 creep？有一个专门函数 getObjectsByPrototype：返回场上所有指定原型（也可以理解为类）的游戏对象数组。比如选出所有 creep：

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep } from 'game/prototypes';

export function loop() {
    var creeps = getObjectsByPrototype(Creep);
}
```

本关还有一个 **Flag**（旗子）对象，它有自己的原型名。

## 本关目标

用 `moveTo` 让你的 creep 走到旗子上。

## 官方示例代码（JavaScript）

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep, Flag } from 'game/prototypes';

export function loop() {
    var creeps = getObjectsByPrototype(Creep);
    var flags = getObjectsByPrototype(Flag);
    creeps[0].moveTo(flags[0]);
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

public final class Main {
    @JSExport
    public static void loop() {
        List<Creep> creeps = Game.getObjectsByPrototype(Creep.class);
        List<Flag> flags = Game.getObjectsByPrototype(Flag.class);
        creeps.get(0).moveTo(flags.get(0));
    }
}
```

- `getObjectsByPrototype(Creep)` → `Game.getObjectsByPrototype(Creep.class)`，返回 `List<Creep>`；取第一只用 `.get(0)`。
- 库里有现成的 `GameObjects.myCreeps()` / `GameObjects.enemyCreeps()` / `GameObjects.flags()`，不想自己筛时可以直接用。
- `moveTo` 在 Java 里返回 `ErrorCode`；示例忽略了返回值，想检查可以写 `if (creep.moveTo(flag) == ErrorCode.ERR_NOT_IN_RANGE) { ... }`。

[← 第 1 关](01-loop-and-import.md) | [返回目录](README.md) | [第 3 关 →](03-first-attack.md)
