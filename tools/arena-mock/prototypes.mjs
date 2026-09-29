// Offline stand-in for the built-in "/game/prototypes" module.
// Mirrors the shapes documented in typed-screeps-arena dist/game/prototypes/*.d.ts.
//
// 行为是"够用的近似"：能走、能打、能孵化、能建、能搬运；常量数值以客户端自带
// arena-docs 的渲染值为准（见 iterations/ideas/009），但引擎细节仍是近似 ——
// 离线模拟器只用来验证 API 用法和策略能不能跑起来。

import { world } from "./world.mjs";
import { getTerrainAt } from "./utils.mjs";
import {
  ATTACK_POWER,
  BODYPART_COST,
  BODYPART_HITS,
  BUILD_POWER,
  CARRY_CAPACITY,
  CONTAINER_CAPACITY,
  CONTAINER_HITS,
  CREEP_SPAWN_TIME,
  DIRECTION_OFFSET,
  ERR_BUSY,
  ERR_FULL,
  ERR_INVALID_ARGS,
  ERR_INVALID_TARGET,
  ERR_NOT_ENOUGH_ENERGY,
  ERR_NOT_ENOUGH_RESOURCES,
  ERR_NOT_IN_RANGE,
  ERR_NO_BODYPART,
  ERR_TIRED,
  EXTENSION_HITS,
  EXTENSION_ENERGY_CAPACITY,
  HARVEST_POWER,
  HEAL_POWER,
  OK,
  RAMPART_HITS,
  RANGED_ATTACK_DISTANCE_RATE,
  RANGED_ATTACK_POWER,
  RANGED_HEAL_POWER,
  RESOURCE_ENERGY,
  ROAD_HITS,
  ROAD_HITS_ON_SWAMP,
  SPAWN_ENERGY_CAPACITY,
  SPAWN_HITS,
  TERRAIN_SWAMP,
  TERRAIN_WALL,
  TOWER_CAPACITY,
  TOWER_COOLDOWN,
  TOWER_ENERGY_COST,
  TOWER_FALLOFF,
  TOWER_FALLOFF_RANGE,
  TOWER_HITS,
  TOWER_OPTIMAL_RANGE,
  TOWER_POWER_ATTACK,
  TOWER_POWER_HEAL,
  TOWER_RANGE,
  WALL_HITS,
} from "./constants.mjs";

/**
 * 造一个 store。
 *
 * 真游戏里的 store 是"属性 + 方法"的混合体：既能直接读 `store.energy`，又有
 * getCapacity/getUsedCapacity/getFreeCapacity 三个方法。容量要按对象类型给
 * （creep 看 CARRY 部件数量，spawn 是 1000，塔只有 10……）。
 */
export function makeStore(capacity, initial = 0) {
  const store = { [RESOURCE_ENERGY]: initial };
  const servesEnergy = (resource) => resource === undefined || resource === RESOURCE_ENERGY;
  store.getCapacity = (resource) => (servesEnergy(resource) ? capacity : null);
  store.getUsedCapacity = (resource) => (servesEnergy(resource) ? store[RESOURCE_ENERGY] : null);
  store.getFreeCapacity = (resource) =>
    servesEnergy(resource) ? Math.max(0, capacity - store[RESOURCE_ENERGY]) : null;
  return store;
}

/** 挡住路的建筑；路、容器、城垛可以踩。 */
const BLOCKING = new Set([
  "StructureWall",
  "StructureSpawn",
  "StructureTower",
  "StructureExtension",
]);

/** 对目标造成伤害，打光了就移出对局。 */
export function dealDamage(target, amount) {
  if (!target || amount <= 0) {
    return;
  }
  target.hits = Math.max(0, target.hits - amount);
  if (target.hits === 0) {
    world.remove(target);
  }
}

/** 往地上放资源；同一格已有同类资源就并进去，和真机一样不会铺一地零散资源。 */
function dropOnGround(x, y, resourceType, amount) {
  if (amount <= 0) {
    return;
  }
  const existing = world.objects.find(
    (object) =>
      object instanceof Resource &&
      object.exists &&
      object.x === x &&
      object.y === y &&
      object.resourceType === resourceType
  );
  if (existing) {
    existing.amount += amount;
  } else {
    world.add(new Resource(undefined, x, y, resourceType, amount));
  }
}

export class GameObject {
  constructor(id, x, y) {
    this.id = id ?? world.newId("obj");
    this.x = x;
    this.y = y;
    this.exists = true;
  }

  getRangeTo(pos) {
    return Math.max(Math.abs(this.x - pos.x), Math.abs(this.y - pos.y));
  }

  toJSON() {
    return { id: this.id, x: this.x, y: this.y };
  }
}

export class Structure extends GameObject {
  constructor(id, x, y, hits, hitsMax) {
    super(id, x, y);
    this.hits = hits;
    this.hitsMax = hitsMax;
  }
}

export class OwnedStructure extends Structure {
  constructor(id, x, y, hits, hitsMax, my) {
    super(id, x, y, hits, hitsMax);
    this.my = my;
  }
}

export class Creep extends GameObject {
  constructor(id, x, y, my, body = []) {
    super(id, x, y);
    this.my = my;
    this.body = body.map((type) => ({ type, hits: BODYPART_HITS }));
    this.hits = this.body.length * BODYPART_HITS;
    this.hitsMax = this.hits;
    this.fatigue = 0;
    /** 孵化中的 creep 为 true；出场（进入 world.objects）时置回 false。 */
    this.spawning = false;
    // 背包大小按 CARRY 部件数量算，和游戏规则一致：一个 CARRY 背 50。
    this.store = makeStore(CARRY_CAPACITY * this.activeParts("carry"));
  }

  hasBodyPart(type) {
    return this.body.some((part) => part.hits > 0 && part.type === type);
  }

  /** 还活着的某种部件有几个，攻击力就是按这个乘的。 */
  activeParts(type) {
    return this.body.filter((part) => part.hits > 0 && part.type === type).length;
  }

  move(direction) {
    const offset = DIRECTION_OFFSET[direction];
    if (!offset) {
      return ERR_INVALID_ARGS;
    }
    return this.stepTo(this.x + offset[0], this.y + offset[1]);
  }

  /** 离线版用 mock 的 A* 走一步；游戏里这一步是引擎自己做的真寻路。 */
  moveTo(target) {
    const dx = Math.sign(target.x - this.x);
    const dy = Math.sign(target.y - this.y);
    if (dx === 0 && dy === 0) {
      return OK;
    }
    const attempts = [
      [dx, dy],
      [dx, 0],
      [0, dy],
    ];
    for (const [ax, ay] of attempts) {
      if (ax === 0 && ay === 0) {
        continue;
      }
      if (this.stepTo(this.x + ax, this.y + ay) === OK) {
        return OK;
      }
    }
    return ERR_INVALID_ARGS;
  }

  stepTo(x, y) {
    if (x < 0 || y < 0 || x >= 100 || y >= 100) {
      return ERR_INVALID_ARGS;
    }
    if (getTerrainAt({ x, y }) === TERRAIN_WALL) {
      return ERR_INVALID_ARGS;
    }
    const blocking = world.objects.some(
      (object) =>
        object !== this &&
        object.exists &&
        object.x === x &&
        object.y === y &&
        !(object instanceof Creep) &&
        BLOCKING.has(object.constructor.name)
    );
    if (blocking) {
      return ERR_INVALID_ARGS;
    }
    this.x = x;
    this.y = y;
    return OK;
  }

  attack(target) {
    if (this.getRangeTo(target) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    if (!this.hasBodyPart("attack")) {
      return ERR_NO_BODYPART;
    }
    dealDamage(target, ATTACK_POWER * this.activeParts("attack"));
    return OK;
  }

  rangedAttack(target) {
    if (this.getRangeTo(target) > 3) {
      return ERR_NOT_IN_RANGE;
    }
    if (!this.hasBodyPart("ranged_attack")) {
      return ERR_NO_BODYPART;
    }
    dealDamage(target, this.rangedPower(this.getRangeTo(target)));
    return OK;
  }

  rangedMassAttack() {
    if (!this.hasBodyPart("ranged_attack")) {
      return ERR_NO_BODYPART;
    }
    for (const object of [...world.objects]) {
      if (object instanceof Creep && object.my !== this.my && this.getRangeTo(object) <= 3) {
        dealDamage(object, this.rangedPower(this.getRangeTo(object)));
      }
    }
    return OK;
  }

  /** 远程攻击力：距离越远伤害越低，官方公式见 RANGED_ATTACK_DISTANCE_RATE。 */
  rangedPower(range) {
    const rate = RANGED_ATTACK_DISTANCE_RATE[range] ?? 0;
    return Math.round(RANGED_ATTACK_POWER * rate * this.activeParts("ranged_attack"));
  }

  heal(target) {
    if (!this.hasBodyPart("heal")) {
      return ERR_NO_BODYPART;
    }
    if (this.getRangeTo(target) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    applyHeal(target, HEAL_POWER * this.activeParts("heal"));
    return OK;
  }

  rangedHeal(target) {
    if (!this.hasBodyPart("heal")) {
      return ERR_NO_BODYPART;
    }
    if (this.getRangeTo(target) > 3) {
      return ERR_NOT_IN_RANGE;
    }
    applyHeal(target, RANGED_HEAL_POWER * this.activeParts("heal"));
    return OK;
  }

  harvest(source) {
    if (!this.hasBodyPart("work")) {
      return ERR_NO_BODYPART;
    }
    if (this.getRangeTo(source) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    const harvested = Math.min(HARVEST_POWER * this.activeParts("work"), source.energy);
    if (harvested <= 0) {
      return ERR_NOT_ENOUGH_RESOURCES;
    }
    source.energy -= harvested;
    // 真游戏：背包有空位就装进去，没空位的那部分掉在地上（不是采不到）。
    const stored = Math.min(harvested, this.store.getFreeCapacity());
    this.store[RESOURCE_ENERGY] = (this.store[RESOURCE_ENERGY] ?? 0) + stored;
    if (harvested > stored) {
      dropOnGround(this.x, this.y, RESOURCE_ENERGY, harvested - stored);
    }
    return OK;
  }

  transfer(target, resourceType, amount) {
    if (this.getRangeTo(target) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    const available = this.store[resourceType] ?? 0;
    const free = target.store ? target.store.getFreeCapacity(resourceType) : 0;
    if (free <= 0) {
      return ERR_FULL;
    }
    const moved = Math.min(amount ?? available, available, free);
    if (moved <= 0) {
      return ERR_NOT_ENOUGH_RESOURCES;
    }
    this.store[resourceType] = available - moved;
    target.store[resourceType] = (target.store[resourceType] ?? 0) + moved;
    return OK;
  }

  withdraw(target, resourceType, amount) {
    if (this.getRangeTo(target) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    const available = target.store[resourceType] ?? 0;
    const free = this.store.getFreeCapacity(resourceType);
    const moved = Math.min(amount ?? available, available, free);
    if (moved <= 0) {
      return available <= 0 ? ERR_NOT_ENOUGH_RESOURCES : ERR_FULL;
    }
    target.store[resourceType] = available - moved;
    this.store[resourceType] = (this.store[resourceType] ?? 0) + moved;
    return OK;
  }

  drop(resourceType, amount) {
    const available = this.store[resourceType] ?? 0;
    const dropped = Math.min(amount ?? available, available);
    if (dropped <= 0) {
      return ERR_NOT_ENOUGH_RESOURCES;
    }
    this.store[resourceType] = available - dropped;
    dropOnGround(this.x, this.y, resourceType, dropped);
    return OK;
  }

  pickup(resource) {
    if (this.getRangeTo(resource) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    if (this.store.getFreeCapacity(resource.resourceType) <= 0) {
      return ERR_FULL;
    }
    this.store[resource.resourceType] =
      (this.store[resource.resourceType] ?? 0) + resource.amount;
    world.remove(resource);
    return OK;
  }

  /**
   * 拖着另一只 creep 走。
   *
   * <p>离线版只做"把目标往拉的人身边拖一格"这个最小语义；真机是目标整段跟随，
   * 具体移动由引擎处理。返回码和官方一致：目标不是 creep → ERR_INVALID_TARGET，
   * 不相邻 → ERR_NOT_IN_RANGE。
   */
  pull(target) {
    if (!(target instanceof Creep)) {
      return ERR_INVALID_TARGET;
    }
    if (this.getRangeTo(target) > 1) {
      return ERR_NOT_IN_RANGE;
    }
    const dx = Math.sign(this.x - target.x);
    const dy = Math.sign(this.y - target.y);
    if (dx !== 0 || dy !== 0) {
      const nextX = target.x + dx;
      const nextY = target.y + dy;
      // 不能拖到拉的人自己站的格子上；等拉的人先走一步，下一 tick 再跟。
      if (nextX !== this.x || nextY !== this.y) {
        target.stepTo(nextX, nextY);
      }
    }
    return OK;
  }

  build(site) {
    if (!this.hasBodyPart("work")) {
      return ERR_NO_BODYPART;
    }
    if (this.getRangeTo(site) > 3) {
      return ERR_NOT_IN_RANGE;
    }
    const spent = Math.min(BUILD_POWER, this.store[RESOURCE_ENERGY] ?? 0);
    if (spent <= 0) {
      return ERR_NOT_ENOUGH_RESOURCES;
    }
    this.store[RESOURCE_ENERGY] -= spent;
    site.progress = Math.min(site.progressTotal, site.progress + spent);
    return OK;
  }
}

function applyHeal(target, amount) {
  target.hits = Math.min(target.hitsMax, target.hits + amount);
}

export class StructureSpawn extends OwnedStructure {
  constructor(id, x, y, my) {
    super(id, x, y, SPAWN_HITS, SPAWN_HITS, my);
    this.store = makeStore(SPAWN_ENERGY_CAPACITY, SPAWN_ENERGY_CAPACITY);
    this.spawning = undefined;
    /** setDirections 设置过的新兵出场方向；没设置过时是空数组。 */
    this.directions = [];
  }

  spawnCreep(body) {
    if (this.spawning) {
      return { error: ERR_BUSY };
    }
    if (!Array.isArray(body) || body.length === 0 || body.length > 50) {
      return { error: ERR_INVALID_ARGS };
    }
    const cost = body.reduce((sum, type) => sum + (BODYPART_COST[type] ?? 0), 0);
    // 真机：能量从"我方全部 spawn 和 extension"里一起扣，不是只看 spawn 自己。
    const wallets = world.objects.filter(
      (object) =>
        object.exists &&
        object.my === this.my &&
        object.store &&
        (object instanceof StructureSpawn || object instanceof StructureExtension)
    );
    const total = wallets.reduce((sum, wallet) => sum + (wallet.store[RESOURCE_ENERGY] ?? 0), 0);
    if (total < cost) {
      return { error: ERR_NOT_ENOUGH_ENERGY };
    }
    let left = cost;
    for (const wallet of [this, ...wallets.filter((wallet) => wallet !== this)]) {
      const taken = Math.min(left, wallet.store[RESOURCE_ENERGY] ?? 0);
      wallet.store[RESOURCE_ENERGY] -= taken;
      left -= taken;
      if (left === 0) {
        break;
      }
    }

    const spot = this.freeSpot();
    const creep = new Creep(undefined, spot.x, spot.y, this.my, body);
    // 真机上孵化中的 creep 还没有 id（官方 typings 写 number|string，实际上这里给 undefined）。
    // 离线必须照着来，否则绑定库里"没有 id 的对象"这条路径永远测不到。
    creep.id = undefined;
    creep.spawning = true;
    // 真机是每个部件 3 tick（CREEP_SPAWN_TIME），不是一 tick 一个。
    const needTime = body.length * CREEP_SPAWN_TIME;
    this.spawning = {
      needTime,
      remainingTime: needTime,
      creep,
      cancel: () => {
        this.spawning = undefined;
        return OK;
      },
    };
    return { object: creep };
  }

  /** 孵化完成的 creep 从这一刻才真正进入对局。 */
  tick() {
    if (!this.spawning) {
      return;
    }
    this.spawning.remainingTime -= 1;
    if (this.spawning.remainingTime > 0) {
      return;
    }
    const creep = this.spawning.creep;
    this.spawning = undefined;
    // 真正进入对局的那一刻才拿到 id。
    creep.id = world.newId("creep");
    creep.spawning = false;
    world.add(creep);
  }

  freeSpot() {
    for (let direction = 1; direction <= 8; direction += 1) {
      const [dx, dy] = DIRECTION_OFFSET[direction];
      const x = this.x + dx;
      const y = this.y + dy;
      if (getTerrainAt({ x, y }) === TERRAIN_WALL) {
        continue;
      }
      if (world.objects.some((object) => object.exists && object.x === x && object.y === y)) {
        continue;
      }
      return { x, y };
    }
    return { x: this.x, y: this.y + 1 };
  }

  setDirections(directions) {
    this.directions = Array.isArray(directions) ? [...directions] : [];
    return OK;
  }
}

export class StructureTower extends OwnedStructure {
  constructor(id, x, y, my) {
    super(id, x, y, TOWER_HITS, TOWER_HITS, my);
    this.store = makeStore(TOWER_CAPACITY, TOWER_CAPACITY);
    this.cooldown = 0;
  }

  attack(target) {
    if (this.cooldown > 0) {
      return ERR_TIRED;
    }
    if (!target || !target.exists) {
      return ERR_INVALID_TARGET;
    }
    if (this.getRangeTo(target) > TOWER_RANGE) {
      return ERR_NOT_IN_RANGE;
    }
    if ((this.store[RESOURCE_ENERGY] ?? 0) < TOWER_ENERGY_COST) {
      return ERR_NOT_ENOUGH_ENERGY;
    }
    this.store[RESOURCE_ENERGY] -= TOWER_ENERGY_COST;
    this.cooldown = TOWER_COOLDOWN;
    dealDamage(target, this.powerAt(TOWER_POWER_ATTACK, this.getRangeTo(target)));
    return OK;
  }

  heal(target) {
    if (this.cooldown > 0) {
      return ERR_TIRED;
    }
    if (!(target instanceof Creep) || !target.exists) {
      return ERR_INVALID_TARGET;
    }
    if (this.getRangeTo(target) > TOWER_RANGE) {
      return ERR_NOT_IN_RANGE;
    }
    if ((this.store[RESOURCE_ENERGY] ?? 0) < TOWER_ENERGY_COST) {
      return ERR_NOT_ENOUGH_ENERGY;
    }
    this.store[RESOURCE_ENERGY] -= TOWER_ENERGY_COST;
    this.cooldown = TOWER_COOLDOWN;
    applyHeal(target, this.powerAt(TOWER_POWER_HEAL, this.getRangeTo(target)));
    return OK;
  }

  /**
   * 塔的威力随距离线性衰减：optimal 范围内满威力，之后按
   * {@code falloff / (falloffRange - optimal)} 的比例递减。
   *
   * <p>本关（Arena）的值是 optimal=1、falloffRange=21、falloff=1：
   * 攻击 1000 起步、每格 -50；治疗 600 起步、每格 -30。
   */
  powerAt(base, range) {
    if (range <= TOWER_OPTIMAL_RANGE) {
      return base;
    }
    const rate =
      1 - ((range - TOWER_OPTIMAL_RANGE) * TOWER_FALLOFF) / (TOWER_FALLOFF_RANGE - TOWER_OPTIMAL_RANGE);
    return Math.max(0, Math.round(base * rate));
  }

  tick() {
    this.cooldown = Math.max(0, this.cooldown - 1);
  }
}

export class StructureExtension extends OwnedStructure {
  constructor(id, x, y, my) {
    super(id, x, y, EXTENSION_HITS, EXTENSION_HITS, my);
    this.store = makeStore(EXTENSION_ENERGY_CAPACITY);
  }
}

export class StructureContainer extends OwnedStructure {
  constructor(id, x, y, my) {
    super(id, x, y, CONTAINER_HITS, CONTAINER_HITS, my);
    this.store = makeStore(CONTAINER_CAPACITY);
  }
}

export class StructureRampart extends OwnedStructure {
  constructor(id, x, y, my) {
    super(id, x, y, RAMPART_HITS, RAMPART_HITS, my);
  }
}

export class StructureWall extends Structure {
  constructor(id, x, y) {
    super(id, x, y, WALL_HITS, WALL_HITS);
  }
}

export class StructureRoad extends Structure {
  constructor(id, x, y, hits = ROAD_HITS) {
    super(id, x, y, hits, hits);
  }
}

/**
 * 工地完工时按原型名造出实体建筑。
 *
 * <p>真机语义：进度满的那一刻工地消失、建筑出现；路建在沼泽上是 2500 耐久（平地 500）。
 * 原型名认不出来时返回 undefined，让调用方把工地留在原地——宁可暴露问题，也不悄悄吞掉。
 */
function structureFromSite(name, x, y, my) {
  switch (name) {
    case "StructureTower":
      return new StructureTower(undefined, x, y, my);
    case "StructureSpawn":
      return new StructureSpawn(undefined, x, y, my);
    case "StructureExtension":
      return new StructureExtension(undefined, x, y, my);
    case "StructureContainer":
      return new StructureContainer(undefined, x, y, my);
    case "StructureRampart":
      return new StructureRampart(undefined, x, y, my);
    case "StructureWall":
      return new StructureWall(undefined, x, y);
    case "StructureRoad": {
      const hits = getTerrainAt({ x, y }) === TERRAIN_SWAMP ? ROAD_HITS_ON_SWAMP : ROAD_HITS;
      return new StructureRoad(undefined, x, y, hits);
    }
    default:
      return undefined;
  }
}

export class ConstructionSite extends GameObject {
  constructor(id, x, y, structurePrototypeName, my, progressTotal = 300) {
    super(id, x, y);
    this.progress = 0;
    this.progressTotal = progressTotal;
    this.structurePrototypeName = structurePrototypeName;
    this.structure = undefined;
    this.my = my;
  }

  /** 进度满的那一 tick，工地换成实体建筑（和真机一致，工地本身消失）。 */
  tick() {
    if (this.progress < this.progressTotal) {
      return;
    }
    const built = structureFromSite(this.structurePrototypeName, this.x, this.y, this.my);
    if (!built) {
      return;
    }
    world.remove(this);
    world.add(built);
  }

  remove() {
    world.remove(this);
  }
}

export class Resource extends GameObject {
  constructor(id, x, y, resourceType, amount) {
    super(id, x, y);
    this.resourceType = resourceType;
    this.amount = amount;
  }
}

export class Source extends GameObject {
  constructor(id, x, y, energy = 1000) {
    super(id, x, y);
    this.energy = energy;
    this.energyCapacity = 1000;
  }

  /** 能量慢慢回复，和游戏里的 SOURCE_ENERGY_REGEN 一个意思。 */
  tick() {
    this.energy = Math.min(this.energyCapacity, this.energy + 10);
  }
}

/**
 * 旗子。真游戏里它挂在 /game/prototypes 上（每个关卡的官方 typings 都带它），
 * 不在 /arena 里——那个模块只有部分关卡才有。
 *
 * 中立旗子身上根本没有 my 属性——这正是 Java 侧判断"中立"的依据。
 */
export class Flag extends GameObject {
  constructor(id, x, y, my) {
    super(id, x, y);
    if (my !== undefined) {
      this.my = my;
    }
  }
}

/** 掉在地上的身体部件（进阶 Capture the Flag 用）。 */
export class BodyPart extends GameObject {
  constructor(id, x, y, type) {
    super(id, x, y);
    this.type = type;
  }
}
