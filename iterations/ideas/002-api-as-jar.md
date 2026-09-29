# 002 把 API 抽成独立 jar / 多模块 Maven

状态：部分已落地（绑定库可 `mvn install` 当依赖用；多模块拆分未做，见文末"进展"）

## 背景

v0.1 是**单模块 Maven**：`screeps.arena.*`（绑定库）和 `bot.Main`（策略）在同一个模块里，
一次 `mvn package` 同时编译两者并产出 `main.mjs`。

## 想法

拆成多模块：

```
screeps-arena-java/          父 pom
  arena-java-api/            screeps.arena.*  →  发布成 jar
  bot-example/               依赖 api，写策略，产出 main.mjs
```

## 收益

- API 有了版本号，可以被别人依赖
- 策略和绑定库可以分别演进；换 bot 不用重新编译 API
- 更接近官方那几个 starter 之外"正经语言绑定库"的形态

## 代价

- 跨模块构建更慢，改一行 API 要重新安装 jar 才能被 bot 看到
- 需要处理版本管理（SNAPSHOT / release）
- TeaVM 的 classpath 配置要同时覆盖两个模块的 `target/classes`，需要验证插件是否支持

## 迁移步骤

1. 把 `pom.xml` 改成 `<packaging>pom</packaging>` 的父 pom，保留公共属性与插件版本
2. 新建 `arena-java-api` 模块，把 `src/main/java/screeps/arena/**` 整体搬过去
3. 新建 `bot-example` 模块，`bot/Main.java` 搬过去，依赖 `arena-java-api`
4. TeaVM 插件配置留在 `bot-example` 里，确保 classpath 包含 api 模块的输出目录
5. 验证 `dist/<关卡>/main.mjs` 产物与单模块时一致（用离线模拟器做回归）

## 进展（2026-09-29）

一部分已经落地，只是路径和当初设想的不太一样：

- 绑定库已经能 `mvn install` 进本地仓库
  （`io.github.screepsarena:screeps-arena-java:0.1.0-SNAPSHOT`），外部工程可以直接依赖它，
  再在自己的 pom 里加 TeaVM 插件产出 `main.mjs`
- 第一个真实消费者是一个 IntelliJ 工程（包名 `cn.net.zhu.demo`，采集策略）：
  依赖这个 jar，用工程里的 `build.cmd` 一次 `mvn package` 就能在自己目录里产出 `main.mjs`
- **多模块拆分还没做**：现在安装出去的 jar 里同时装着 `bot.*` 示例策略，体积偏大也不干净。
  要拆就按上面"迁移步骤"那一节走，让 `arena-java-api` 只装 `screeps.arena.**`
- 当初担心的"TeaVM 跨模块 classpath"已经顺带验证过：TeaVM 插件会把工程的依赖 classpath
  一起喂给编译器（`compileScopes` 默认含 `provided`），jar 里的 class 和 `@JSBodyImport`
  注解都能正常处理，多模块拆分不会卡在这一步
