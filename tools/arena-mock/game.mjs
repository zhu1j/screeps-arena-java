// Offline stand-in for the built-in "/game" root module.

export * as constants from "./constants.mjs";
export * as prototypes from "./prototypes.mjs";
export * as utils from "./utils.mjs";
export * as pathFinder from "./path-finder.mjs";
export * as visual from "./visual.mjs";

export const arenaInfo = {
  name: "Spawn and Swamp",
  level: 1,
  season: "alpha",
  ticksLimit: 2000,
  cpuTimeLimit: 1,
  cpuTimeLimitFirstTick: 5,
};
