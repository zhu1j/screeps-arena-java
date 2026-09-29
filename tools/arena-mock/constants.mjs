// Offline stand-in for the arena's built-in "/game/constants" module.
// 数值以游戏客户端自带的 arena-docs 常量页为准（constants.d.ts 里有个别字面值是旧的）。
// 核对方法：在客户端 resources/app/arena-docs/index.html 里搜常量名，页面渲染的就是运行时值。

// --- Body parts ---
export const MOVE = "move";
export const WORK = "work";
export const CARRY = "carry";
export const ATTACK = "attack";
export const RANGED_ATTACK = "ranged_attack";
export const TOUGH = "tough";
export const HEAL = "heal";

export const BODYPART_COST = {
  [MOVE]: 50,
  [WORK]: 100,
  [CARRY]: 50,
  [ATTACK]: 80,
  [RANGED_ATTACK]: 150,
  [TOUGH]: 10,
  [HEAL]: 250,
};
export const BODYPART_HITS = 100;
export const MAX_CREEP_SIZE = 50;
/** 每孵一个身体部件要几 tick（孵化一只 creep 的时长 = 部件数 × 这个值）。 */
export const CREEP_SPAWN_TIME = 3;

// --- Directions ---
export const TOP = 1;
export const TOP_RIGHT = 2;
export const RIGHT = 3;
export const BOTTOM_RIGHT = 4;
export const BOTTOM = 5;
export const BOTTOM_LEFT = 6;
export const LEFT = 7;
export const TOP_LEFT = 8;

/** [dx, dy] indexed by direction constant. */
export const DIRECTION_OFFSET = {
  [TOP]: [0, -1],
  [TOP_RIGHT]: [1, -1],
  [RIGHT]: [1, 0],
  [BOTTOM_RIGHT]: [1, 1],
  [BOTTOM]: [0, 1],
  [BOTTOM_LEFT]: [-1, 1],
  [LEFT]: [-1, 0],
  [TOP_LEFT]: [-1, -1],
};

// --- Terrain ---
export const TERRAIN_PLAIN = 0;
export const TERRAIN_WALL = 1;
export const TERRAIN_SWAMP = 2;

// --- Return codes ---
export const OK = 0;
export const ERR_NOT_OWNER = -1;
export const ERR_NO_PATH = -2;
export const ERR_NAME_EXISTS = -3;
export const ERR_BUSY = -4;
export const ERR_NOT_FOUND = -5;
export const ERR_NOT_ENOUGH_RESOURCES = -6;
export const ERR_NOT_ENOUGH_ENERGY = -6;
export const ERR_NOT_ENOUGH_EXTENSIONS = -6;
export const ERR_INVALID_TARGET = -7;
export const ERR_FULL = -8;
export const ERR_NOT_IN_RANGE = -9;
export const ERR_INVALID_ARGS = -10;
export const ERR_TIRED = -11;
export const ERR_NO_BODYPART = -12;

// --- Resources ---
export const RESOURCE_ENERGY = "energy";
export const RESOURCES_ALL = [RESOURCE_ENERGY];
export const RESOURCE_DECAY = 1000;
export const SOURCE_ENERGY_REGEN = 10;

// --- Combat tuning ---
export const ATTACK_POWER = 30;
export const RANGED_ATTACK_POWER = 10;
export const HEAL_POWER = 12;
export const RANGED_HEAL_POWER = 4;
/** 下标就是距离：rate[1]=1、rate[2]=0.4、rate[3]=0.1（距离 0 不可能出现在攻击里）。 */
export const RANGED_ATTACK_DISTANCE_RATE = [1, 1, 0.4, 0.1];
export const DISMANTLE_POWER = 50;
export const DISMANTLE_COST = 0.005;
export const HARVEST_POWER = 2;
export const BUILD_POWER = 5;
export const REPAIR_POWER = 100;
export const REPAIR_COST = 0.01;

// --- Structures ---
export const STRUCTURE_SPAWN = "spawn";
export const STRUCTURE_TOWER = "tower";
export const STRUCTURE_EXTENSION = "extension";
export const STRUCTURE_WALL = "constructedWall";
export const STRUCTURE_CONTAINER = "container";
export const STRUCTURE_RAMPART = "rampart";
export const STRUCTURE_ROAD = "road";

export const STRUCTURE_PROTOTYPES = {
  StructureTower: STRUCTURE_TOWER,
  StructureSpawn: STRUCTURE_SPAWN,
  StructureRoad: STRUCTURE_ROAD,
  StructureRampart: STRUCTURE_RAMPART,
  StructureExtension: STRUCTURE_EXTENSION,
  StructureWall: STRUCTURE_WALL,
  StructureContainer: STRUCTURE_CONTAINER,
};

export const SPAWN_HITS = 3000;
export const SPAWN_ENERGY_CAPACITY = 1000;
export const CARRY_CAPACITY = 50;
export const EXTENSION_HITS = 100;
export const EXTENSION_ENERGY_CAPACITY = 100;
export const TOWER_HITS = 3000;
export const TOWER_CAPACITY = 10;
export const TOWER_ENERGY_COST = 10;
export const TOWER_RANGE = 20;
export const TOWER_COOLDOWN = 10;
export const TOWER_OPTIMAL_RANGE = 1;
export const TOWER_FALLOFF_RANGE = 21;
export const TOWER_FALLOFF = 1;
export const TOWER_POWER_ATTACK = 1000;
export const TOWER_POWER_HEAL = 600;
export const TOWER_POWER_REPAIR = 200;
export const CONTAINER_HITS = 300;
export const CONTAINER_CAPACITY = 2000;
export const RAMPART_HITS = 10000;
export const RAMPART_HITS_MAX = 10000;
export const WALL_HITS = 10000;
export const WALL_HITS_MAX = 10000;
export const ROAD_HITS = 500;
/** 沼泽上的路更结实（structure 页面：平地 500、沼泽 2500）。 */
export const ROAD_HITS_ON_SWAMP = 2500;
export const ROAD_WEAROUT = 1;
export const MAX_CONSTRUCTION_SITES = 10;

export const CONSTRUCTION_COST = {
  StructureTower: 1250,
  StructureExtension: 200,
  StructureRoad: 10,
  StructureContainer: 100,
  StructureWall: 100,
  StructureRampart: 200,
  StructureSpawn: 3000,
};

export const CONSTRUCTION_COST_ROAD_SWAMP_RATIO = 5;
export const CONSTRUCTION_COST_ROAD_WALL_RATIO = 150;
