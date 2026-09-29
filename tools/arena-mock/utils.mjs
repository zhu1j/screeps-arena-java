// Offline stand-in for the built-in "/game/utils" module.

import { world } from "./world.mjs";
import {
  CONSTRUCTION_COST,
  CONSTRUCTION_COST_ROAD_SWAMP_RATIO,
  DIRECTION_OFFSET,
  ERR_FULL,
  ERR_INVALID_ARGS,
  ERR_INVALID_TARGET,
  MAX_CONSTRUCTION_SITES,
  TERRAIN_SWAMP,
  TERRAIN_WALL,
} from "./constants.mjs";
import { searchPath } from "./path-finder.mjs";
import { getTerrainAt, setTerrain } from "./terrain.mjs";
// 循环 import：prototypes.mjs 会从这里拿 getTerrainAt，但只在函数体里用，
// 模块初始化阶段互不访问，所以是安全的。
import { ConstructionSite } from "./prototypes.mjs";

// 地形和寻路在真实游戏里都属于别的模块，这里转出去只是为了让调用方少 import 一个文件。
export { getTerrainAt, setTerrain, searchPath };

export function getTicks() {
  return world.ticks;
}

export function getCpuTime() {
  return 0;
}

export function getObjects() {
  return world.objects.filter((object) => object.exists);
}

export function getObjectsByPrototype(prototype) {
  return world.objects.filter((object) => object.exists && object instanceof prototype);
}

export function getObjectById(id) {
  // 故意严判（===）、不帮忙转类型：这是真机可能给出的最坏情形。
  // 数字 id 的对象用字符串查不到时，绑定库 Js.objectById 会再用数字查一次兜底，
  // 这条兜底路径由 tools/tests/probe.mjs + MOCK_NUMERIC_IDS=1 覆盖。
  return world.objects.find((object) => object.exists && object.id === id) ?? null;
}

export function getRange(a, b) {
  return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
}

export const getDistance = getRange;

export function getDirection(dx, dy) {
  for (const [direction, [ox, oy]] of Object.entries(DIRECTION_OFFSET)) {
    if (Math.sign(dx) === ox && Math.sign(dy) === oy) {
      return Number(direction);
    }
  }
  return undefined;
}

export function findInRange(fromPos, positions, range) {
  return positions.filter((pos) => getRange(fromPos, pos) <= range);
}

export function findClosestByRange(fromPos, positions) {
  let best;
  let bestRange = Infinity;
  for (const pos of positions) {
    const range = getRange(fromPos, pos);
    if (range < bestRange) {
      bestRange = range;
      best = pos;
    }
  }
  return best ?? null;
}

export function findClosestByPath(fromPos, positions) {
  return findClosestByRange(fromPos, positions);
}

export function findPath(fromPos, toPos) {
  return searchPath(fromPos, toPos, {}).path;
}

/**
 * 在指定位置放工地。签名和游戏一致：{x, y} 坐标 + 建筑原型构造函数。
 *
 * @returns 成功时 `{ object }`（ConstructionSite 实例），失败时 `{ error }`。
 */
export function createConstructionSite(pos, structurePrototype) {
  if (!pos || typeof pos.x !== "number" || typeof pos.y !== "number"
      || typeof structurePrototype !== "function") {
    return { error: ERR_INVALID_ARGS };
  }
  const terrain = getTerrainAt(pos);
  if (terrain === TERRAIN_WALL) {
    return { error: ERR_INVALID_TARGET };
  }
  const occupied = world.objects.some(
    (object) => object.exists && object.x === pos.x && object.y === pos.y
  );
  if (occupied) {
    return { error: ERR_INVALID_TARGET };
  }
  const mySites = world.objects.filter(
    (object) => object.exists && object instanceof ConstructionSite && object.my === true
  ).length;
  if (mySites >= MAX_CONSTRUCTION_SITES) {
    return { error: ERR_FULL };
  }
  const name = structurePrototype.name;
  let cost = CONSTRUCTION_COST[name] ?? 300;
  // 路修在沼泽上要贵 5 倍（平地 10、沼泽 50）。
  if (name === "StructureRoad" && terrain === TERRAIN_SWAMP) {
    cost *= CONSTRUCTION_COST_ROAD_SWAMP_RATIO;
  }
  const site = new ConstructionSite(
    undefined, pos.x, pos.y, name, true, cost
  );
  world.add(site);
  return { object: site };
}
