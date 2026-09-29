# 004 产物体积与 CPU 预算

状态：想法 · 未排期（v0.1 先量化，不优化）

## 已知数据

SIMPLE 优化等级下，只调一次 `getTicks()` 并打一条日志的最小程序：

- 产物 `main.mjs` **约 40 KB**（其中绝大部分是 TeaVM 运行时）

加上完整 API 层和一个真实策略后，估计落在 **150–300 KB**。

## 要查清的事

- Arena 对 `main.mjs` 有没有体积上限？如果有，是多少？（Screeps World 是 5 MB，Arena 未确认）
- 竞技场的 CPU 是**墙钟时间**限制（`arenaInfo.cpuTimeLimit` 给的是每 tick 的墙钟秒数），
  不是抽象 CPU 点数。这意味着生成代码的常数因子直接换算成可用操作数

## 可能的优化手段

1. **优化等级**：`optimizationLevel` 从 `SIMPLE` 提到 `ADVANCED` 或 `FULL`
2. **压缩**：`minifying=true`，配合 `maxTopLevelNames` 之类参数
3. **裁剪 API**：只把真正用到的原型编进去（TeaVM 会做可达性分析，但反射/多态会扩大可达集）
4. **避免昂贵的 Java 设施**：热路径上不要用 `Optional`、`Stream`、装箱集合
5. **包装对象做 identity cache**：同一个游戏对象每 tick 复用同一个包装实例，避免反复分配

## 注意

优化等级提高会**拉长构建时间**，也会让生成的 JS 更难读。建议默认保持 SIMPLE
便于调试，等确认体积或 CPU 真的有问题再逐级提升，并把对比数据记在这里。

## 调试

竞技场里拿不到 source map（官方 TypeScript starter 的 README 也提到过这点），
所以线上报错只能看到编译后的栈。离线模拟器要考虑输出可映射回 Java 行号的信息。
