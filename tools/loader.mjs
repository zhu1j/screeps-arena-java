// Node module-resolution hook that maps the arena's absolute module specifiers
// ("/game/utils", "/arena", ...) onto the offline mock files in this folder.

import { pathToFileURL } from "node:url";

const MOCKS = {
  "/game": "./arena-mock/game.mjs",
  "/game/constants": "./arena-mock/constants.mjs",
  "/game/prototypes": "./arena-mock/prototypes.mjs",
  "/game/utils": "./arena-mock/utils.mjs",
  "/game/path-finder": "./arena-mock/path-finder.mjs",
  "/game/visual": "./arena-mock/visual.mjs",
  "/arena": "./arena-mock/arena.mjs",
  "/arena/season_beta/capture_the_flag/basic/prototypes": "./arena-mock/arena.mjs",
};

export async function resolve(specifier, context, nextResolve) {
  const mapped = MOCKS[specifier];
  if (mapped) {
    return { url: new URL(mapped, import.meta.url).href, shortCircuit: true };
  }
  return nextResolve(specifier, context);
}

export { MOCKS };
export const mockUrl = (specifier) => new URL(MOCKS[specifier], import.meta.url).href;
export const pathToUrl = pathToFileURL;
