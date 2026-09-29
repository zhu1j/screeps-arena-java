# 006 关卡专属模块（/arena）与多关卡产物

状态：部分已落地（`BodyPart` 的显式登记已实现），其余未排期

## 背景

一度以为 `/arena` 是一个像 `/game` 那样的全局模块，只是"导出什么随关卡变化"。
上真机前翻官方关卡定义（游戏目录下的 `resources/app/arena-definitions/`）才确认不是：

- `common/typings/` 里只有 `game`
- 每个关卡定义自带 `typings/<赛季>/<关卡>/<难度>/...`，对应模块名是
  `arena/<赛季>/<关卡>/<难度>` 和 `arena/<赛季>/<关卡>/<难度>/prototypes`
- 官方关卡模板的 import 写成 `from 'arena/season_beta/capture_the_flag/basic'`

也就是说：**关卡模块不是全局的，路径里带着赛季和难度**。产物里出现一个该关卡没有的模块
import，ESM 在加载阶段就整体失败——表现是日志面板一片空白、整局毫无动作，
而且编译期不报任何错。

顺带确认的两件事：`Flag` 在 `/game/prototypes` 上（每个关卡的官方 typings 都导出它）；
`arenaInfo` 在 `/game` 上，而且不一定每个关卡都有，读的时候要判空。
真正属于关卡模块的常见原型只有 `BodyPart`，以及 Collect & Control 的
`ScoreCollector`、`AreaEffect`、`StructureGoal` 之类。

## 已经落地的部分

- `Flag` 改从 `/game/prototypes` 取，不再碰 `/arena`
- `BodyPart` 改成显式登记：`GameObjects.registerBodyPart()`。产物里的 import 是按调用关系
  生成的，所以只有真的调了登记，那份产物的 TeaVM 输出里才会出现 `/arena`
- `tools/sim.mjs` 加静态检查：产物引用了 `/arena` 而该关卡没有这个模块，直接判失败

## 还没做的

1. **绑上其它关卡模块**。Collect & Control 的 `ScoreCollector` 要写到赛季和难度
   （`arena/season_beta/collect_and_control/basic/prototypes`），而赛季是会换的。可选做法：
   - 按关卡分编译单元，把模块路径做成 Maven 属性（`-Darena.scenario.module=...`），
     构建时生成一个带 `@JSBodyImport` 的类——省事，但要求一个关卡一次构建
   - 把手写 `@JSBodyImport` 的适配类放进按关卡分的源码目录
     （`src/main/scenario/<关卡>`），用 Maven profile 切换——更像正经 Java 工程，配置更重
2. **登记 API 的形态**。现在是 `registerBodyPart()` 这种具体方法，关卡多了应该收敛成
   `GameObjects.register(Class, 原型, 包装函数)` 之类的通用入口。
3. **赛季升级的应对**。赛季更新后模块路径会变，绑定库要能只改配置、不改策略代码。
   目前的想法是把"关卡适配"独立成包（`screeps.arena.scenario.ctf`），策略只依赖包名，
   不依赖模块字符串。

## 参考

- 官方关卡定义：`<游戏目录>/resources/app/arena-definitions/<关卡 id>/typings/...`
- 社区 typings（typed-screeps-arena）的 `dist__arena__*` 文件，同样是"一个关卡一个模块"
