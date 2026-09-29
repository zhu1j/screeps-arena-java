// 绑定库自检 bot 的离线回归：跑 dist/probe/main.mjs，断言那些"关卡用不到、
// 但绑定层必须做对"的角落（getObjectById、孵化中无 id 的实例复用、类型识别、
// 各种 String 取值、旗子三态）。
//
//   node --import ./tools/register.mjs tools/tests/probe.mjs

import { existsSync } from "node:fs";
import { world } from "../arena-mock/world.mjs";
import { setTerrain } from "../arena-mock/terrain.mjs";
import { TERRAIN_PLAIN } from "../arena-mock/constants.mjs";
import {
  ConstructionSite, Creep, Flag, Resource, Source, StructureSpawn,
} from "../arena-mock/prototypes.mjs";
import { arenaInfo } from "../arena-mock/game.mjs";

const moduleUrl = new URL("../../dist/probe/main.mjs", import.meta.url);
if (!existsSync(moduleUrl)) {
  console.error(`找不到自检产物：${moduleUrl.pathname}（先 mvn package）`);
  process.exit(2);
}

let captured = [];
const realLog = console.log;
console.log = (...args) => { captured.push(args.join(" ")); };

world.reset();
setTerrain(() => TERRAIN_PLAIN);
arenaInfo.name = "Probe";
const spawn = world.add(new StructureSpawn(undefined, 25, 25, true));
spawn.store.energy = 300;
world.add(new Creep(undefined, 25, 26, true, ["work", "carry", "move"]));
world.add(new Creep(undefined, 24, 25, true, ["move"]));
world.add(new Source(undefined, 28, 25));
world.add(new Resource(undefined, 26, 26, "energy", 50));
world.add(new ConstructionSite(undefined, 26, 25, "StructureTower", true, 5000));
world.add(new Flag(undefined, 25, 28, true));
world.add(new Flag(undefined, 25, 22, false));
world.add(new Flag(undefined, 25, 30));

const module_ = await import(moduleUrl.href);
for (let tick = 1; tick <= 5; tick += 1) {
  world.advance();
  module_.loop();
}
console.log = realLog;

const has = (line) => captured.includes(line);
const startsWith = (prefix) => captured.some((line) => line.startsWith(prefix));
const spawnId = captured.find((line) => line.startsWith("probe: spawn id=")) ?? "";
const adopted = captured.find((line) => line.startsWith("probe: adopted=")) ?? "";

const checks = [
  ["spawn id 读得到", spawnId !== "" && !spawnId.endsWith("=null") && !spawnId.endsWith("=undefined")],
  ["getObjectById 找回同一个实例", has("probe: byId=same")],
  ["查不到的 id 返回 null", has("probe: byId missing=null")],
  ["spawnCreep 成功", has("probe: spawnCreep ok=true")],
  ["孵化中的 creep 没有 id", has("probe: spawning id=null")],
  ["孵化中的对象是同一个 Java 实例", has("probe: spawning instance=same")],
  ["工地原型名", has("probe: prototype=StructureTower")],
  ["creep.body 部件类型", has("probe: bodypart=WORK hits=100")],
  ["掉落资源类型", has("probe: resource=energy amount=50")],
  ["旗子三态", has("probe: flags mine=1 neutral=1 enemy=1")],
  ["Visual 是类（new + 链式画图）", has("probe: visual layer=5 size=0")],
  ["spawn directions 默认空", has("probe: directions before=0")],
  ["spawn setDirections 生效", has("probe: directions after=2")],
  ["Store 无参容量查询", has("probe: store capacity=50 free=50 used=0")],
  ["ticksToDecay 缺省为 -1", has("probe: decay=-1")],
  ["creep.pull", has("probe: pull=OK")],
  ["孵化中的 creep spawning=true", has("probe: spawning flag=true")],
  ["出场的 creep spawning=false", has("probe: creep spawning=false")],
  ["孵化完成后认领回同一实例", adopted !== "" && !adopted.startsWith("probe: adopted=no")],
];

console.log("========== 绑定库自检（probe bot） ==========");
let failed = 0;
for (const [name, ok] of checks) {
  if (!ok) {
    failed += 1;
  }
  console.log(`${ok ? "PASS" : "FAIL"}  ${name}`);
}
if (failed > 0) {
  console.log("--- 捕获到的 probe 日志 ---");
  for (const line of captured) {
    console.log(`  ${line}`);
  }
}
console.log(`---------- ${checks.length - failed}/${checks.length} 通过 ----------`);
process.exit(failed === 0 ? 0 : 1);
