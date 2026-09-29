// 教程 10 关离线行为测试：为每一关搭一个小场景，跑真实产物的 loop()，断言过关条件。
//
//   node --import ./tools/register.mjs tools/tests/tutorial.mjs [产物根目录]
//
// 产物根目录是装着 level01..level10 的那一层（即构建产出的 dist/tutorial）。
// 不传就依次试 TUTORIAL_DIR 环境变量、仓库自己的 dist/tutorial。
// 设 MOCK_NUMERIC_IDS=1 会把 mock 的 id 切成数字，模拟真机 id 是 number|string 的行为。

import { existsSync } from "node:fs";
import { resolve } from "node:path";
import { pathToFileURL } from "node:url";
import { world } from "../arena-mock/world.mjs";
import { setTerrain } from "../arena-mock/terrain.mjs";
import { TERRAIN_PLAIN } from "../arena-mock/constants.mjs";
import {
  ConstructionSite, Creep, Flag, Source, StructureContainer, StructureSpawn, StructureTower,
} from "../arena-mock/prototypes.mjs";

const BASE = process.argv[2] ?? process.env.TUTORIAL_DIR ?? resolve("dist/tutorial");
if (!existsSync(`${BASE}/level01/main.mjs`)) {
  console.error(`找不到教程产物：${BASE}/level01/main.mjs`);
  console.error("先构建（mvn package），或把 dist/tutorial 目录路径作为参数传进来。");
  process.exit(2);
}

let captured = [];
const realLog = console.log;
console.log = (...args) => { captured.push(args.join(" ")); };
const out = (line) => process.stdout.write(line + "\n");

const results = [];

async function scenario(n, name, options) {
  captured = [];
  world.reset();
  setTerrain(() => TERRAIN_PLAIN);
  try {
    const nn = String(n).padStart(2, "0");
    const ctx = options.setup() ?? {};
    const mod = await import(pathToFileURL(`${BASE}/level${nn}/main.mjs`).href);
    if (typeof mod.loop !== "function") {
      throw new Error("没有导出 loop()");
    }
    for (let tick = 1; tick <= options.ticks; tick++) {
      world.advance();
      if (options.eachTick) options.eachTick(ctx, tick);
      mod.loop();
    }
    const problem = options.expect(ctx);
    results.push({ n, name, ok: !problem, detail: problem ?? "通过" });
  } catch (error) {
    results.push({ n, name, ok: false, detail: `异常：${error.message}` });
  }
}

const myCreeps = () => world.objects.filter((o) => o instanceof Creep && o.my === true);
const enemyCreeps = () => world.objects.filter((o) => o instanceof Creep && o.my === false);
const onFlag = (creep, flag) => creep.x === flag.x && creep.y === flag.y;

// ---- 1. Loop and import：每 tick 一条日志 ----
await scenario(1, "Loop and import", {
  ticks: 5,
  setup: () => ({}),
  expect: () => (captured.length >= 5 ? null : `只捕获到 ${captured.length} 条日志`),
});

// ---- 2. Simple move：creep 走到旗子 ----
await scenario(2, "Simple move", {
  ticks: 20,
  setup: () => {
    const creep = world.add(new Creep(undefined, 20, 20, true, ["move"]));
    const flag = world.add(new Flag(undefined, 25, 20, true));
    return { creep, flag };
  },
  expect: ({ creep, flag }) =>
    onFlag(creep, flag) ? null : `creep 停在 (${creep.x}, ${creep.y})，没到旗子 (${flag.x}, ${flag.y})`,
});

// ---- 3. First attack：追上并打死敌人 ----
await scenario(3, "First attack", {
  ticks: 40,
  setup: () => {
    world.add(new Creep(undefined, 20, 20, true, ["move", "attack"]));
    const enemy = world.add(new Creep(undefined, 24, 20, false, ["move"]));
    return { enemy };
  },
  expect: ({ enemy }) => (enemy.exists ? `敌人还剩 ${enemy.hits} hits` : null),
});

// ---- 4. Creeps bodies：三兵种配合 + 治疗 ----
await scenario(4, "Creeps bodies", {
  ticks: 80,
  setup: () => {
    const melee = world.add(new Creep(undefined, 20, 20, true, ["move", "attack"]));
    melee.hits = 60;                      // 假装挨了打，考一考治疗逻辑
    world.add(new Creep(undefined, 20, 24, true, ["move", "ranged_attack"]));
    world.add(new Creep(undefined, 20, 22, true, ["move", "heal"]));
    const enemy = world.add(new Creep(undefined, 26, 22, false, ["move", "attack"]));
    return { melee, enemy };
  },
  expect: ({ melee, enemy }) => {
    if (enemy.exists) return `敌人还剩 ${enemy.hits} hits`;
    if (!melee.exists) return "近战手阵亡了";
    return null;
  },
});

// ---- 5. Store and transfer：搬能量 → 塔开火 ----
await scenario(5, "Store and transfer", {
  ticks: 60,
  setup: () => {
    const tower = world.add(new StructureTower(undefined, 25, 25, true));
    tower.store.energy = 0;
    const container = world.add(new StructureContainer(undefined, 23, 23, true));
    container.store.energy = 200;
    world.add(new Creep(undefined, 24, 25, true, ["move", "carry"]));
    const enemy = world.add(new Creep(undefined, 35, 25, false, ["move"]));
    return { tower, enemy };
  },
  expect: ({ tower, enemy }) => {
    if (enemy.exists) return `敌人还剩 ${enemy.hits} hits（塔能量=${tower.store.energy}）`;
    return null;
  },
});

// ---- 6. Terrain：每只 creep 站上自己的旗子 ----
await scenario(6, "Terrain", {
  ticks: 30,
  setup: () => {
    const creepA = world.add(new Creep(undefined, 20, 20, true, ["move"]));
    const creepB = world.add(new Creep(undefined, 20, 26, true, ["move"]));
    const flagA = world.add(new Flag(undefined, 30, 20, true));
    const flagB = world.add(new Flag(undefined, 30, 26, true));
    return { creepA, creepB, flagA, flagB };
  },
  expect: ({ creepA, creepB, flagA, flagB }) => {
    const onAny = (creep) => (onFlag(creep, flagA) ? flagA : onFlag(creep, flagB) ? flagB : null);
    const targetA = onAny(creepA);
    const targetB = onAny(creepB);
    if (!targetA) return `creepA 停在 (${creepA.x}, ${creepA.y})，没站上旗子`;
    if (!targetB) return `creepB 停在 (${creepB.x}, ${creepB.y})，没站上旗子`;
    if (targetA === targetB) return "两只 creep 站在同一面旗上";
    return null;
  },
});

// ---- 7. Spawn creeps：孵两只，各走一面旗 ----
await scenario(7, "Spawn creeps", {
  ticks: 40,
  setup: () => {
    const spawn = world.add(new StructureSpawn(undefined, 25, 25, true));
    spawn.store.energy = 100;
    const flagA = world.add(new Flag(undefined, 30, 30, true));
    const flagB = world.add(new Flag(undefined, 30, 20, true));
    return { spawn, flagA, flagB };
  },
  expect: ({ flagA, flagB }) => {
    const born = myCreeps();
    if (born.length < 2) return `只孵出 ${born.length} 只`;
    const onA = born.filter((c) => onFlag(c, flagA)).length;
    const onB = born.filter((c) => onFlag(c, flagB)).length;
    if (onA + onB < 2) return `到旗子上的只有 ${onA + onB} 只`;
    return null;
  },
});

// ---- 8. Harvest energy：挖矿→送回的循环 ----
await scenario(8, "Harvest energy", {
  ticks: 400,
  setup: () => {
    const spawn = world.add(new StructureSpawn(undefined, 25, 28, true));
    spawn.store.energy = 0;
    world.add(new Source(undefined, 25, 25));
    world.add(new Creep(undefined, 25, 26, true, ["work", "carry", "move"]));
    return { spawn };
  },
  expect: ({ spawn }) =>
    spawn.store.energy >= 300 ? null : `400 tick 只送回了 ${spawn.store.energy} 能量`,
});

// ---- 9. Construction：取能量 → 放工地 → 建到完工 ----
await scenario(9, "Construction", {
  ticks: 1600,
  setup: () => {
    world.add(new Creep(undefined, 48, 55, true, ["work", "carry", "move"]));
    const container = world.add(new StructureContainer(undefined, 52, 55, true));
    container.store.energy = 5000;
    return {};
  },
  expect: () => {
    // 真机语义：建完的那一刻工地消失、Tower 出现。
    const site = world.objects.find((o) => o instanceof ConstructionSite);
    if (site) return `工地还没建完：${site.progress}/${site.progressTotal}`;
    const tower = world.objects.find(
      (o) => o instanceof StructureTower && o.x === 50 && o.y === 55 && o.my === true
    );
    if (!tower) return "工地消失后没有变成 Tower";
    if (tower.hits !== tower.hitsMax) return `Tower 耐久异常：${tower.hits}/${tower.hitsMax}`;
    return null;
  },
});

// ---- 10. Final test：经济 + 补兵 + 消灭全部敌人 ----
await scenario(10, "Final test", {
  ticks: 1600,
  setup: () => {
    const spawn = world.add(new StructureSpawn(undefined, 25, 25, true));
    spawn.store.energy = 300;
    world.add(new Source(undefined, 28, 25));
    world.add(new Creep(undefined, 40, 25, false, ["move", "attack"]));
    world.add(new Creep(undefined, 42, 27, false, ["move", "attack"]));
    world.add(new Creep(undefined, 38, 23, false, ["move", "attack"]));
    return { spawn };
  },
  expect: ({ spawn }) => {
    const left = enemyCreeps().length;
    if (left > 0) return `还剩 ${left} 个敌人（我方=${myCreeps().length}，spawn 能量=${spawn.store.energy}）`;
    if (myCreeps().length < 2) return "敌人死光了，但自己人也全没了";
    return null;
  },
});

console.log = realLog;
out("========== 教程 10 关离线行为测试 ==========");
for (const r of results) {
  out(`${r.ok ? "PASS" : "FAIL"}  level${String(r.n).padStart(2, "0")} ${r.name.padEnd(20)} ${r.detail}`);
}
const failed = results.filter((r) => !r.ok).length;
out(`---------- ${results.length - failed}/${results.length} 通过 ----------`);
process.exit(failed === 0 ? 0 : 1);
