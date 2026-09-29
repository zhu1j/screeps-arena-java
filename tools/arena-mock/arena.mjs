// Offline stand-in for the built-in "/arena" module.
//
// 真游戏里这个模块不是每个关卡都有的（tutorial、Spawn and Swamp 就没有），导出什么也随
// 关卡变化：基础 CTF 有 BodyPart，Collect & Control 换成 ScoreCollector。Flag 不在这里，
// 它在 /game/prototypes 上，每个关卡都有。

export { BodyPart } from "./prototypes.mjs";
