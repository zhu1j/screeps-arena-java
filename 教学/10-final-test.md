# 第 10 关：Final test（期末考试）

> 原文：客户端 `src/mocks/tutorial/10-final-test.template.html`（游戏内教程页面）。

现在你已经学会了如何控制 creep、攻击敌人、使用能量、孵化新的 creep、建造新的建筑。准备好迎接期末考了吗？

这一关有一个 Spawn 和一个带能量的 Source。地图另一侧有一群敌方 creep，很快就会来进攻。你需要孵化工人 creep、采集能量、再孵化更多 creep，消灭他们。而且——这一关我们连示例代码都不给了！

完成这个教程后，你就可以进入真正的 PvP 竞技场，和其他玩家多人对战了。祝你好运，玩得开心！

## 本关目标

顶住进攻、消灭所有敌方 creep，完成教程。

（原页面此处有演示视频）

## Java 写法提示

- 这一关是对前面所有内容的综合：用 `static` 字段记录工人/士兵、`spawn.isSpawning()` 防重复孵化、采集能量、按距离判断敌人并让士兵上前。
- 可以参考本仓库 `src/main/java/bot/` 下的示例：`bot.harvest.Main` 是采集循环，`bot.spawnswamp.Main` 是带兵的进阶版，对照着改出你自己的策略。
- 真机调试先看日志：`Game.log(...)` 会出现在对局日志面板里；日志完全不出现说明代码没加载成功，和策略本身无关。

[← 第 9 关](09-construction.md) | [返回目录](README.md)
