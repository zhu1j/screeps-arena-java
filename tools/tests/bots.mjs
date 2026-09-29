// 仓库自带三个 bot 的离线回归：每个都在 mock 场景里跑固定 tick 数，
// 断言"没抛异常、并且真的动起来了"。
//
//   node --import ./tools/register.mjs tools/tests/bots.mjs [tick 数]
//
// 前提：先 mvn package，产物在 dist/<bot>/main.mjs。
// 设 MOCK_NUMERIC_IDS=1 会把 mock 的 id 切成数字，模拟真机 id 是 number|string 的行为。

import { readFile } from "node:fs/promises";
import { world } from "../arena-mock/world.mjs";
import { SCENARIOS } from "../arena-mock/scenarios.mjs";
import { Creep } from "../arena-mock/prototypes.mjs";
import { arenaInfo } from "../arena-mock/game.mjs";

const ticks = Number(process.argv[2] ?? 300);

const BOTS = [
  { arena: "spawn-and-swamp", minOwnCreeps: 1, bootstrap: () => {} },
  // harvest 的场景在 sim 里是 0 能量起步（方便看循环）；测试里补一笔启动资金，好让工人孵得出来。
  { arena: "harvest", minOwnCreeps: 1, bootstrap: (spawn) => { spawn.store.energy = 300; } },
  { arena: "capture-the-flag", minOwnCreeps: 1, bootstrap: () => {} },
];

const results = [];
for (const bot of BOTS) {
  const scenario = SCENARIOS[bot.arena];
  try {
    // 产物的模块自检和 tools/sim.mjs 一致：没有 "/arena" 模块的关卡不能引用它。
    const source = await readFile(scenario.module, "utf8");
    const arenaImport = /from\s*["']\/arena["']/.exec(source);
    if (arenaImport && !scenario.arenaModule) {
      throw new Error(`产物引用了 ${arenaImport[0]}，但这个关卡没有 "/arena" 模块`);
    }

    world.reset();
    arenaInfo.name = bot.arena;
    const spawn = scenario.setup();
    bot.bootstrap(spawn);

    const module_ = await import(scenario.module.href);
    for (let tick = 1; tick <= ticks; tick += 1) {
      world.advance();
      scenario.eachTick(spawn);
      module_.loop();
    }

    const creeps = world.objects.filter((o) => o instanceof Creep && o.my === true).length;
    if (creeps < bot.minOwnCreeps) {
      throw new Error(`${ticks} tick 后只有 ${creeps} 只自己的 creep`);
    }
    results.push({ arena: bot.arena, ok: true, detail: `${ticks} tick，我方 creep=${creeps}` });
  } catch (error) {
    results.push({ arena: bot.arena, ok: false, detail: error.message });
  }
}

console.log("========== bot 离线回归 ==========");
for (const result of results) {
  console.log(`${result.ok ? "PASS" : "FAIL"}  ${result.arena.padEnd(18)} ${result.detail}`);
}
const failed = results.filter((result) => !result.ok).length;
console.log(`---------- ${results.length - failed}/${results.length} 通过 ----------`);
process.exit(failed === 0 ? 0 : 1);
