# 关卡 01：Loop and import（循环与导入）

> 先讲 Java 怎么写，再讲游戏里这一关要做什么。
> 前置：[01-原理](01-原理.md)、[02-API速查](02-API速查.md)。

## 先学 Java：本关用到的 API

- **入口方法**：游戏每 tick 调用一次 `loop()`；绑定库要求写成 `@JSExport public static void loop()`。
- `Game.getTicks()` → 当前 tick（`int`）。
- `Game.log(String)` → 写游戏日志面板（相当于官方的 `console.log`）。
- Java 字符串拼接：`"Current tick: " + Game.getTicks()` 会自动把数字转成字符串。

## 完整策略（可直接编译）

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

## 再看游戏：这一关要做什么

- 地图完全是空的，没有任何游戏对象；只要 `loop` 每 tick 输出一点东西就能通关。
- 教程默认代码目录：`%USERPROFILE%\ScreepsArena\tutorial`（关卡界面左下角可改绑）。

## 跑起来

1. 把类放进你的工程。用 IDEA 的话写你自己的包（例如 `cn.net.zhu.demo`）；本文的示例不带 `package`，放在源码根目录也能编译。
2. 构建出 `main.mjs`（本仓库 `mvn package`，或你自己的工程打包）。
3. 游戏里把代码目录指向产物文件夹，点 PLAY。
4. 预期日志：

   ```
   Current tick: 1
   Current tick: 2
   ...
   ```

## 坑与差异

- Java 没有 `console.log`；`System.out.println` 在真机上**什么都看不到**，日志一律用 `Game.log`。
- `loop()` 必须叫这个名字、`public static void`、带 `@JSExport`；改名字游戏就找不到入口。
- 想跨 tick 保存数据，用 `static` 字段（第 07 关细讲）。

[返回目录](README.md) | [关卡 02 →](关卡02-Simple-move.md)
