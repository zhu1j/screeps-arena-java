// Registers the arena module-resolution hook. Use with:
//   node --import ./tools/register.mjs ./tools/sim.mjs

import { register } from "node:module";

register("./loader.mjs", import.meta.url);
