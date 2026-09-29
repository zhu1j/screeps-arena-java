// 仓库自带 bot 的离线场景：给 tools/sim.mjs（人工看跑得怎么样）和
// tools/tests/bots.mjs（自动断言）共用，避免两边各写一份、慢慢跑偏。

import { Creep, Flag, Source, StructureSpawn } from "./prototypes.mjs";
import { TERRAIN_PLAIN, TERRAIN_SWAMP } from "./constants.mjs";
import { setTerrain } from "./terrain.mjs";
import { world } from "./world.mjs";

/** 产物路径跟着这个文件走，换电脑/换目录都不用改。 */
const dist = (name) => new URL(`../../dist/${name}/main.mjs`, import.meta.url);

const worldAdd = (object) => world.add(object);

export const SCENARIOS = {
  "spawn-and-swamp": {
    module: dist("spawn-and-swamp"),
    arenaModule: false,
    setup() {
      const spawn = worldAdd(new StructureSpawn(undefined, 25, 25, true));
      worldAdd(new StructureSpawn(undefined, 25, 10, false));
      worldAdd(new Creep(undefined, 21, 25, false, ["move", "attack"]));
      worldAdd(new Source(undefined, 30, 25));
      // 中间横一条沼泽带，让寻路有东西可绕。
      setTerrain((pos) => (pos.y >= 18 && pos.y <= 22 ? TERRAIN_SWAMP : TERRAIN_PLAIN));
      return spawn;
    },
    // 游戏里能量要靠自己采，这里每 tick 直接往里补一点，好让孵化能持续跑下去。
    eachTick(spawn) {
      spawn.store.energy = Math.min(300, spawn.store.energy + 30);
    },
  },

  // 采集型 bot：只有自己的 spawn 和一个能量矿，没有敌人，方便看清"挖矿 → 回家"的循环。
  "harvest": {
    module: dist("harvest"),
    arenaModule: false,
    setup() {
      const spawn = worldAdd(new StructureSpawn(undefined, 25, 25, true));
      worldAdd(new Source(undefined, 28, 25));
      setTerrain(() => TERRAIN_PLAIN);
      return spawn;
    },
    eachTick() {
      // 故意不补能量：spawn 的能量只会被孵化消耗，再由 worker 搬回来。
    },
  },

  "capture-the-flag": {
    module: dist("capture-the-flag"),
    arenaModule: true,
    setup() {
      const spawn = worldAdd(new StructureSpawn(undefined, 25, 25, true));
      worldAdd(new StructureSpawn(undefined, 25, 8, false));
      worldAdd(new Flag(undefined, 25, 28, true));
      worldAdd(new Flag(undefined, 25, 5, false));
      worldAdd(new Flag(undefined, 25, 16));
      worldAdd(new Creep(undefined, 25, 19, false, ["move", "attack"]));
      setTerrain(() => TERRAIN_PLAIN);
      return spawn;
    },
    eachTick(spawn) {
      spawn.store.energy = Math.min(300, spawn.store.energy + 40);
    },
  },
};
