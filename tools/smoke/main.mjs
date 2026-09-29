// 真机排查用的最小产物：纯 JavaScript，不经过 TeaVM，也不碰 screeps.arena 绑定库。
//
// 把游戏客户端的代码目录临时指向 tools\smoke，就只剩两件最简单的事：客户端能不能读到
// main.mjs，以及 loop() 有没有被调用。如果这里都打不出日志，问题就不在我们的产物里；
// 如果这里正常、dist 里的产物没日志，那问题一定在 TeaVM 产物那一侧。
//
// 顺带把当前关卡里各内置模块导出了什么符号打出来——各关卡的原型集合不一样，
// 这份清单就是本地和真机对答案的依据。

import * as utils from "/game/utils";
import * as prototypes from "/game/prototypes";
import * as constants from "/game/constants";
import * as visual from "/game/visual";
import * as arena from "/arena";
import { arenaInfo } from "/game";

let ticks = 0;

export function loop() {
    ticks++;

    if (ticks === 1) {
        console.log("[smoke] main.mjs 已加载");
        console.log("[smoke] arenaInfo=" + JSON.stringify(arenaInfo));
        console.log("[smoke] 命名空间类型：utils=" + typeof utils
                + " prototypes=" + typeof prototypes
                + " constants=" + typeof constants
                + " visual=" + typeof visual
                + " arena=" + typeof arena);
        console.log("[smoke] /game/prototypes 导出：" + Object.keys(prototypes).sort().join(","));
        console.log("[smoke] /arena 导出：" + Object.keys(arena).sort().join(","));
    }

    // 每 tick 都真的调一次全局函数：模块加载成功不等于里面的东西能用。
    if (ticks % 10 === 0) {
        console.log("[smoke] tick=" + ticks + " getTicks()=" + utils.getTicks());
    }
}
