# 003 扩展到进阶关卡与赛季模式

状态：想法 · 未排期

## 背景

v0.1 的范围是 `/game` 核心原型 + 基础关卡（Spawn & Swamp、基础 CTF）。

## 待覆盖的部分

**进阶 CTF**

- `Flag` 的进阶版本（与基础版签名一致，但需要注意导入路径不同）
- `/arena/season_beta/capture_the_flag/advanced/prototypes`

**Collect & Control**

- `ScoreCollector`（`resourceType` / `my` / `score` / `scoreTotal`）
- `AreaEffect`（`effect` 为 `"freeze"` 或 `"damage"`）
- 常量：`RESOURCE_SCORE_X/Y/Z`、`RESOURCE_SCORE`、`SCORE_COLLECTOR_*` 一族

**`/game/visual`** —— 调试可视化，v0.1 可以只做最小子集

## 需要注意的坑

`/arena` 这个根模块**导出什么取决于当前加载的是哪个关卡**。基础 CTF 里 `Flag` 存在，
Collect & Control 里 `ScoreCollector` 存在，但两边不一定都有。

这意味着 Java 侧不能无脑在同一个类里静态导入所有关卡的原型 —— 导入一个不存在的
符号，生成的 ESM 会在加载时直接报错（模块级导入是急切的，不是懒加载）。

可行的做法：

1. **按关卡分编译单元**：每个关卡一套 `Arena*` 绑定类，用 Maven 属性选一套编进产物
2. **懒访问**：不在模块顶层 import，改成在 JS body 里 `import()` 动态取
3. **只依赖共有子集**：跨关卡的公共代码只碰 `/game/*`，关卡特有代码单独放

方案 1 最符合"一个关卡一个输出目录"的用法，优先考虑。
