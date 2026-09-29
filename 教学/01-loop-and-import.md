# 第 1 关：Loop and import（循环与导入）

> 原文：客户端 `src/mocks/tutorial/1-loop-and-import.template.html`（游戏内教程页面）。
> 本文为中文翻译整理，术语保留英文；如与游戏内最新文案不一致，以游戏为准。

欢迎来到教程！在这里你会学到如何通过写代码来玩 Screeps Arena。

## 简介

“Screeps”的意思是 “scripting creeps”（用脚本驱动你的小兵）。游戏使用**真正的 JavaScript** 来编写单位（creep）的行为。我们不会教你 JavaScript 本身，但会从最基础的地方开始；如果你想精通这个游戏，建议先熟悉这门语言。如果你希望用其它编程语言，可以看看[这些社区支持的方案](https://steamcommunity.com/sharedfiles/filedetails/?id=3609741226)——本仓库做的 Java 绑定就是其中一个方向。

你的玩法就是在本地的文件夹里写代码文件。游戏内没有代码编辑器，推荐用外部编辑器，比如 [Microsoft VS Code](https://code.visualstudio.com/)。

点击 **PLAY** 按钮时，这些文件会被提交到服务器。只要代码没有语法错误，对局就会开始，你可以观看它跑出来。对局中不能修改已提交的代码——想改就要重开一局。每局只有几秒钟，但你可以用任意速度回放。

## 脚本主循环

每一个回合（我们称作 “tick”），游戏都会执行你的主函数 `loop()`。它已经在 `main.mjs` 里定义好了：

```javascript
export function loop() {
    // 你的代码写在这里
}
```

这个函数里的内容会被反复执行，直到对局结束。注意：你在这里只是**登记**命令——命令真正**执行**是在 `loop` 函数结束之后。

## 导入

在使用游戏对象和方法之前，需要先把它们**导入**（import）。比如有个函数 `getTicks()` 可以取当前 tick，把它从 `game/utils` 模块导入后就能用：

```javascript
import { getTicks } from 'game/utils';

export function loop() {
    console.log('Current tick:', getTicks());
}
```

这样可导入的方法和对象还有很多，完整清单见游戏内文档（顶部 Documentation 按钮）。

注意上面的 `console.log`：它会把任意值输出到游戏控制台面板，方便调试和观察运行中的代码。这个函数不用导入，全局可用。

## 开始吧

教程第一步没有任何游戏对象，地图完全是空的。你要做的只是：在 `loop` 函数里用 `console.log` 往控制台输出一点信息。

把上面的示例代码复制进你的 `main.mjs`，点 **PLAY** 就完成了。

（原页面此处有演示视频）

## Java 写法提示

```java
import org.teavm.jso.JSExport;
import screeps.arena.Game;

public final class Main {
    @JSExport
    public static void loop() {
        Game.log("Current tick: " + Game.getTicks());
    }
}
```

- 官方的 `export function loop()` 对应 `@JSExport public static void loop()`，由 TeaVM 编译成 `main.mjs`。
- `console.log(...)` → `Game.log(...)`；`getTicks()` → `Game.getTicks()`。
- 官方讲的是“从哪个模块 import”；Java 这边照常写 `import` 语句，绑定库会在编译时生成对应的 JS import。

[返回目录](README.md) | [第 2 关 →](02-simple-move.md)
