// Offline test run: sets up a scenario on the mock map, imports the compiled arena module
// and drives loop() for a number of ticks.
//
//   node --import ./tools/register.mjs ./tools/sim.mjs [ticks] [arena] [main.mjs 路径]
//
// arena 可选 spawn-and-swamp（默认）、harvest 和 capture-the-flag，各自对应 dist/ 下的一个产物目录。
// 第 4 个参数可选：直接跑指定的 main.mjs（比如自己工程里产出的那份），场景仍按 arena 准备。

import { world } from "./arena-mock/world.mjs";
import { readFile } from "node:fs/promises";
import { resolve } from "node:path";
import { pathToFileURL } from "node:url";
import { Creep } from "./arena-mock/prototypes.mjs";
import { arenaInfo } from "./arena-mock/game.mjs";
import { SCENARIOS } from "./arena-mock/scenarios.mjs";

const ticks = Number(process.argv[2] ?? 5);
const arena = process.argv[3] ?? "spawn-and-swamp";
const artifact = process.argv[4];

const scenario = SCENARIOS[arena];
if (!scenario) {
  console.error(`未知关卡：${arena}（可选：${Object.keys(SCENARIOS).join("、")}）`);
  process.exit(1);
}

world.reset();
const spawn = scenario.setup();
arenaInfo.name = arena === "capture-the-flag" ? "Capture the Flag" : "Spawn and Swamp";

// 真机模块自检："/arena" 不是每个关卡都有的模块（tutorial、Spawn and Swamp 就没有）。
// 产物里只要出现它的 import，缺模块的关卡在加载阶段就会整体失败，日志面板一片空白——
// 那种失败在离线跑里是看不出来的，所以在这里静态拦一道。
const moduleUrl = artifact ? pathToFileURL(resolve(artifact)) : scenario.module;
const source = await readFile(moduleUrl, "utf8");
const arenaImport = /from\s*["']\/arena["']/.exec(source);
if (arenaImport && !scenario.arenaModule) {
  console.error(
    `!! 产物里有 ${arenaImport[0]}，但「${arena}」关卡没有 "/arena" 模块，真机上会加载失败。`
  );
  process.exit(1);
}
console.log(
  arenaImport ? '模块自检：产物引用了 "/arena"（本关卡有该模块）' : '模块自检：产物没有引用 "/arena"'
);

const arenaModule = await import(moduleUrl.href);
console.log(`--- ${arena}：跑 ${ticks} tick ---`);

for (let tick = 1; tick <= ticks; tick += 1) {
  world.advance();
  scenario.eachTick(spawn);
  arenaModule.loop();
  report(tick);
}

console.log(`--- ${ticks} tick 之后 ---`);
for (const object of world.objects) {
  const side = object.my === undefined ? "中立" : object.my ? "我方" : "敌方";
  console.log(
    `  ${object.constructor.name.padEnd(16)} ${side} (${object.x}, ${object.y})` +
      (object.hits === undefined ? "" : ` hits=${object.hits}`)
  );
}
console.log(`可视化调用：${world.logs.length} 次（bot 自己的日志直接打在输出里）`);

function report(tick) {
  const creeps = (mine) =>
    world.objects.filter((object) => object instanceof Creep && object.my === mine).length;
  const energy = spawn.store.energy ?? 0;
  console.log(
    `[tick ${String(tick).padStart(2)}] 我方 creep=${creeps(true)} 敌方 creep=${creeps(false)} ` +
      `spawn 能量=${energy} 对象数=${world.objects.length}`
  );
}
