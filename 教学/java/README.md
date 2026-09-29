# Screeps: Arena × Java 实操教学

这套文档教你**用 Java** 玩 Screeps: Arena，内容全部结合本仓库（Java 绑定库 + TeaVM 构建链）。
它和隔壁 `教学/` 的分工：那边是游戏内教程原文的中文翻译；这边讲“Java 怎么接入、怎么写策略、怎么跑进游戏”。

建议先读顺序：

1. [01-原理：Java 是怎么变成游戏能跑的代码的](01-原理.md)
2. [02-API 速查：游戏概念 ↔ Java 类/方法](02-API速查.md)
3. [教程通关操作步骤](教程通关步骤.md)：每关绑哪个目录、怎么开局验证（10 关代码已随工程构建好）
4. 教程关卡 01～10（Java 版）详解，见下表

## 用 Java 正常游戏的完整步骤

### 方式 A：直接在本仓库里开发（跑通全流程最快）

1. **装环境**：JDK 17+（本仓库用 JDK 25 验证过）、Maven 3.9+；Node.js 18+ 只在跑离线模拟器/代码监视时需要。
2. **构建**：

   ```powershell
   mvn package
   ```

   产物是每关一个目录：`dist/spawn-and-swamp/main.mjs`、`dist/capture-the-flag/main.mjs`、`dist/harvest/main.mjs`。
   如果 Maven 用的不是 JDK 17+，先设置：

   ```powershell
   $env:JAVA_HOME = "D:\.jdks\corretto-17.0.13"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   mvn package
   ```

3. **先离线试跑**（可选，不开游戏）：

   ```powershell
   node --import ./tools/register.mjs ./tools/sim.mjs 200 spawn-and-swamp
   ```

4. **进游戏绑定目录**：打开关卡 → 左下角代码目录 → 选 `dist\<对应关卡>` 文件夹（客户端要绝对路径）。
   **目录和关卡必须对应**——客户端加载的就是那个目录下的 `main.mjs`。
5. **开一局，看日志**：tick 1 应该出现

   ```
   [screeps-arena-java] 已加载：Spawn and Swamp（最多 2000 tick）
   ```

   之后每 10 tick 一行状态。这两行能出现，就说明 Java 产物被游戏正常加载、`loop()` 每个 tick 都在被调用。
6. **改代码 → 生效**：编辑 `src/main/java/...` 后重新构建（或 `node tools/watch.mjs` 自动重编），
   然后**重开一局**——代码是开局时载入的，对局中改不了。

### 方式 B：在自己的 IDEA 工程里开发（包名、目录都归你）

1. 在绑定库目录把它装进本地 Maven 仓库（以后改绑定库要重装一次）：

   ```powershell
   mvn install
   ```

2. 自己的 `pom.xml` 里加依赖：

   ```xml
   <dependency>
     <groupId>io.github.screepsarena</groupId>
     <artifactId>screeps-arena-java</artifactId>
     <version>0.1.0-SNAPSHOT</version>
   </dependency>
   ```

3. 加 TeaVM 插件 0.15.0 和三个 `provided` 依赖（绑定库不传递它们）：
   `teavm-maven-plugin` + `teavm-classlib` + `teavm-jso` + `teavm-interop`。
   配置照抄本仓库 `pom.xml`，把 `<mainClass>` 换成自己的入口类，
   `<targetDirectory>` 指向游戏要读的那个文件夹。
4. 入口类要求：有一个 `@JSExport public static void loop()` 方法（每 tick 被调用）。
5. `mvn package` → 产出 `main.mjs` → 游戏里把该目录绑上，再按方式 A 的第 5、6 步验证。

### 构建后自查（不开游戏，10 秒验证产物）

构建完想先确认产物本身没问题，可以在仓库根目录跑：

```powershell
node --import ./tools/register.mjs --input-type=module -e "const m = await import('./dist/spawn-and-swamp/main.mjs'); m.loop(); console.log('SELFCHECK OK');"
```

把 `dist/spawn-and-swamp` 换成对应关卡目录即可。它会用离线 mock 加载产物、手动调用一次 `loop()`：

- 打印 `SELFCHECK OK`（以及 bot 自己的 `Game.log` 输出）→ 产物可加载、`loop()` 可执行；
- 报 `Cannot find module '/game/...'` → 忘了 `--import ./tools/register.mjs`；
- 报语法错误（`SyntaxError`）→ 产物坏了或 TeaVM 目标配置不对，回仓库重新 `mvn package`。

想一次把"绑定库 + 三个示例 bot + 教程 10 关"全查一遍，在仓库根目录跑全量回归：

```powershell
node tools/tests/run.mjs "E:\Program Files (x86)\Game\ScreepsArenaDemo\ScreepsArena\dist\tutorial"
```

共 8 组，每一组都在"字符串 id / 数字 id"两种 mock 模式下各跑一遍——
真机上 `id` 是 `number|string`，数字 id 模式就是专门复现真机行为的。

## 关卡索引（Java 版）

| 关 | 文件 | 你会学到 |
|----|------|----------|
| 01 | [Loop and import](关卡01-Loop-and-import.md) | `@JSExport` 入口、`Game.log`、字符串拼接 |
| 02 | [Simple move](关卡02-Simple-move.md) | `List`、`Game.getObjectsByPrototype(Class)`、包装类 |
| 03 | [First attack](关卡03-First-attack.md) | Stream 筛选、`ErrorCode`、把逻辑拆成方法 |
| 04 | [Creeps bodies](关卡04-Creeps-bodies.md) | 遍历、`hasBodyPart`、枚举比较 |
| 05 | [Store and transfer](关卡05-Store-and-transfer.md) | `Store`、`ResourceType`、多分支决策 |
| 06 | [Terrain](关卡06-Terrain.md) | 泛型方法 `findClosestByPath`、`for-each` |
| 07 | [Spawn creeps](关卡07-Spawn-creeps.md) | `static` 字段跨 tick、`List.of`、`SpawnCreepResult` |
| 08 | [Harvest energy](关卡08-Harvest-energy.md) | 两状态搬运逻辑、数值判断 |
| 09 | [Construction](关卡09-Construction.md) | `Point`、`StructureType`、`ConstructionSiteResult` |
| 10 | [Final test](关卡10-Final-test.md) | 综合：工人 + 战士、状态机、参考完整 bot |

## 常见问题

- **日志面板完全空白**：先把代码目录临时指向 `tools\smoke`（纯 JS 最小探针）。
  它也没日志 → 客户端压根没读到 `main.mjs`（检查目录、文件名、关卡是否对应）；
  它有日志 → 问题在 TeaVM 产物里，回到本仓库 `mvn package` 重新构建。
- **目录必须绝对路径**，而且指向包含 `main.mjs` 的那个文件夹（不是它的上一级）。
- **改完没生效**：代码是开局时载入的，构建完要重开一局。
- **教程 10 关的默认目录**在 `%USERPROFILE%\ScreepsArena\tutorial`，关卡界面左下角可改绑任意文件夹。
- **免费 Demo 限制**：只能玩教程，多人竞技场要完整版。
