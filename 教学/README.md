# Screeps: Arena 教程中文教学（第 1–10 关）

把游戏内教程每一关的说明原文抠出来、翻成中文，方便边玩边学。

- 原文来源：游戏客户端内嵌页面（`resources/app/dist/main.js` 里的 `src/mocks/tutorial/*.template.html`）
- 中文为翻译整理，术语保留英文原名（creep、tick、spawn、store……）；如与游戏内最新文案不一致，以游戏为准
- 示例代码保持官方 JavaScript 原样，仅把注释翻成中文；Java 对照写在每篇末尾

## Java 实操版

想直接用 Java 上手（本仓库的核心玩法），看 **[`java/`](java/README.md)**：
那里有适配原理、API 速查，以及每关可编译的 Java 策略和“从构建到进游戏”的完整步骤。

## 关卡总览

| 关 | 英文名 | 中文名 | 目标 |
|----|--------|--------|------|
| 1 | Loop and import | 循环与导入 | 在 `loop()` 里用 `console.log` 输出信息 |
| 2 | Simple move | 简单移动 | 让 creep 用 `moveTo` 走到旗子上 |
| 3 | First attack | 第一次攻击 | 消灭敌方 creep |
| 4 | Creeps bodies | Creep 的身体部件 | 让 ATTACK / RANGED_ATTACK / HEAL 三种 creep 各司其职，击败敌人 |
| 5 | Store and transfer | 储物与搬运 | 给 Tower 充能，远程消灭敌人 |
| 6 | Terrain | 地形 | 把所有 creep 走到各自的旗子 |
| 7 | Spawn creeps | 孵化 creep | 孵化两个 creep，分别走到两面旗子 |
| 8 | Harvest energy | 采集能量 | 采集并运送 1000 能量回 spawn |
| 9 | Construction | 建造 | 建一座 Tower |
| 10 | Final test | 期末考试 | 顶住进攻、消灭所有敌人，通关教程 |

## 文件列表

- [第 1 关：循环与导入](01-loop-and-import.md)
- [第 2 关：简单移动](02-simple-move.md)
- [第 3 关：第一次攻击](03-first-attack.md)
- [第 4 关：Creep 的身体部件](04-creeps-bodies.md)
- [第 5 关：储物与搬运](05-store-and-transfer.md)
- [第 6 关：地形](06-terrain.md)
- [第 7 关：孵化 creep](07-spawn-creeps.md)
- [第 8 关：采集能量](08-harvest-energy.md)
- [第 9 关：建造](09-construction.md)
- [第 10 关：期末考试](10-final-test.md)

## 在游戏里怎么练

1. 教程 10 关默认共用 `%USERPROFILE%\ScreepsArena\tutorial` 这个代码目录；在关卡界面左下角可以改绑任意文件夹。
2. 客户端固定读目录里的 `main.mjs`（ES module），并把该目录下的文件一起提交给服务器。
3. 用本仓库的 Java 绑定库练习时：`mvn package` 生成 `main.mjs`，把代码目录指向产物文件夹（或你自己工程输出 `main.mjs` 的目录）；改完代码要重新构建、重开一局。

## Java 对照速查

| 官方 JavaScript | 本绑定库（Java） |
|----------------|------------------|
| `export function loop() {}` | `@JSExport public static void loop() {}` |
| 模块顶层变量（跨 tick 保存） | 类的 `static` 字段 |
| `getObjectsByPrototype(Creep)` | `Game.getObjectsByPrototype(Creep.class)`，返回 `List<Creep>` |
| `creep.my` | `creep.isMine()` |
| `creep.moveTo(target)` | `creep.moveTo(target)`，返回 `ErrorCode` |
| `ERR_NOT_IN_RANGE` | `ErrorCode.ERR_NOT_IN_RANGE` |
| `RESOURCE_ENERGY` | `ResourceType.ENERGY` |
| `MOVE` / `ATTACK` 等身体部件常量 | `BodyPartType.MOVE` / `BodyPartType.ATTACK` |
| `console.log(...)` | `Game.log(...)` |
| `creep.target = flag`（挂自定义数据） | `static Map<String, Flag>` + `creep.getId()` |

每篇末尾的“Java 写法提示”给出该关 API 的对应写法；完整 API 以 `src/main/java/screeps/arena/` 源码为准。

## 重新生成原文

提取与转换脚本（临时目录，未入库）：`.codex-tmp/extract-tutorial.mjs`、`.codex-tmp/html-to-md.mjs`；
原始 HTML 在 `.codex-tmp/tutorial-raw/`，英文 Markdown 在 `.codex-tmp/tutorial-en/`。
