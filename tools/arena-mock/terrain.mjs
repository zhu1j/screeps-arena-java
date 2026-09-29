// 离线地图的地形状态。
//
// 单独放一个模块，是因为 utils 和 path-finder 都要读地形，而真实游戏里
// 这两者本来就互不依赖 —— 放进任何一个都会绕出循环导入。

import { TERRAIN_PLAIN } from "./constants.mjs";

/** 默认整张图都是平地；测试脚本用 setTerrain 换成别的。 */
let terrainAt = () => TERRAIN_PLAIN;

export function setTerrain(fn) {
  terrainAt = fn;
}

export function getTerrainAt(pos) {
  const value = terrainAt(pos);
  return value === undefined ? TERRAIN_PLAIN : value;
}
