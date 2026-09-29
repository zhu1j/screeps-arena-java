// Offline stand-in for the built-in "/game/path-finder" module.
//
// 一张 100x100 的 A*，够离线跑通"绕开墙和沼泽"以及"逃跑"两种用法。
// 数值和游戏不可能完全一致，重点是把 searchPath 的返回形状和选项语义走通。

import { TERRAIN_WALL } from "./constants.mjs";
import { getTerrainAt } from "./terrain.mjs";

export const PLAIN_COST = 2;
export const SWAMP_COST = 10;

const SWAMP = 2;
const MAX_OPS = 50000;
const SIZE = 100;

/** 代价表：0 表示用默认代价，255 表示不可通行。 */
export class CostMatrix {
  constructor() {
    this._bits = new Uint8Array(SIZE * SIZE);
  }

  set(x, y, cost) {
    this._bits[y * SIZE + x] = cost;
  }

  get(x, y) {
    return this._bits[y * SIZE + x];
  }

  clone() {
    const copy = new CostMatrix();
    copy._bits.set(this._bits);
    return copy;
  }
}

const OFFSETS = {
  1: { dx: 0, dy: -1 },
  2: { dx: 1, dy: -1 },
  3: { dx: 1, dy: 0 },
  4: { dx: 1, dy: 1 },
  5: { dx: 0, dy: 1 },
  6: { dx: -1, dy: 1 },
  7: { dx: -1, dy: 0 },
  8: { dx: -1, dy: -1 },
};

const key = (x, y) => y * SIZE + x;

/** 游戏里的 Goal 可以是位置，也可以是 {pos, range}；这里统一成后者。 */
function normalizeGoals(goal) {
  return (Array.isArray(goal) ? goal : [goal]).map((entry) =>
    entry && typeof entry === "object" && "pos" in entry
      ? { pos: entry.pos, range: entry.range ?? 0 }
      : { pos: entry, range: 0 }
  );
}

/** 和游戏同名的入口：用离线地图的地形。 */
export function searchPath(origin, goal, options = {}) {
  return searchPathWith(getTerrainAt, origin, goal, options);
}

/** 地形由调用方注入，方便别的模块（比如 utils.findPath）复用。 */
export function searchPathWith(terrainAt, origin, goal, options = {}) {
  const goals = normalizeGoals(goal);
  const plainCost = options.plainCost ?? PLAIN_COST;
  const swampCost = options.swampCost ?? SWAMP_COST;
  const maxOps = options.maxOps ?? MAX_OPS;
  const costMatrix = options.costMatrix;
  const flee = options.flee ?? false;

  const stepCost = (x, y) => {
    if (costMatrix && costMatrix.get(x, y) > 0) {
      return costMatrix.get(x, y);
    }
    return terrainAt({ x, y }) === SWAMP ? swampCost : plainCost;
  };
  const passable = (x, y) => {
    if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return false;
    if (terrainAt({ x, y }) === TERRAIN_WALL) return false;
    if (costMatrix && costMatrix.get(x, y) >= 255) return false;
    return true;
  };

  const open = [{ x: origin.x, y: origin.y, g: 0, f: 0, parent: null }];
  const best = new Map([[key(origin.x, origin.y), 0]]);
  let ops = 0;

  while (open.length > 0 && ops < maxOps) {
    ops += 1;
    open.sort((a, b) => a.f - b.f);
    const node = open.shift();
    const atOrigin = node.x === origin.x && node.y === origin.y;

    if (flee ? !atOrigin && !inDanger(node, goals) : isGoal(node, goals)) {
      return { path: reconstruct(node, origin), ops, cost: node.g, incomplete: false };
    }

    for (let direction = 1; direction <= 8; direction += 1) {
      const { dx, dy } = OFFSETS[direction];
      const nx = node.x + dx;
      const ny = node.y + dy;
      if (!passable(nx, ny)) continue;

      const g = node.g + stepCost(nx, ny);
      const existing = best.get(key(nx, ny));
      if (existing !== undefined && existing <= g) continue;
      best.set(key(nx, ny), g);
      open.push({ x: nx, y: ny, g, f: g + weight(flee, heuristic(nx, ny, goals)), parent: node });
    }
  }

  return { path: [], ops, cost: 0, incomplete: true };
}

function weight(flee, value) {
  return flee ? -value : value;
}

function isGoal(node, goals) {
  return goals.some(({ pos, range }) => rangeTo(node, pos) <= range);
}

function inDanger(node, goals) {
  return goals.some(({ pos, range }) => rangeTo(node, pos) <= range);
}

function rangeTo(a, b) {
  return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
}

function heuristic(x, y, goals) {
  let best = Infinity;
  for (const { pos, range } of goals) {
    best = Math.min(best, Math.max(0, rangeTo({ x, y }, pos) - range));
  }
  return best === Infinity ? 0 : best;
}

function reconstruct(node, origin) {
  const path = [];
  let current = node;
  while (current && !(current.x === origin.x && current.y === origin.y)) {
    path.push({ x: current.x, y: current.y });
    current = current.parent;
  }
  return path.reverse();
}
