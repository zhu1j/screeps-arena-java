// 产物冒烟测试：每一关的 main.mjs 能不能加载、空世界下 loop() 能不能跑。
// 行为测试（tools/tests/tutorial.mjs）会搭真实场景，这个只负责"产物本身没坏"。
//
//   node --import ./tools/register.mjs tools/tests/smoke.mjs [产物根目录]

import { existsSync } from "node:fs";
import { resolve } from "node:path";
import { pathToFileURL } from "node:url";

const base = process.argv[2] ?? process.env.TUTORIAL_DIR ?? resolve("dist/tutorial");
if (!existsSync(`${base}/level01/main.mjs`)) {
  console.error(`找不到教程产物：${base}/level01/main.mjs`);
  process.exit(2);
}

let failed = 0;
for (let n = 1; n <= 10; n += 1) {
  const nn = String(n).padStart(2, "0");
  const url = pathToFileURL(`${base}/level${nn}/main.mjs`).href;
  try {
    const module_ = await import(url);
    if (typeof module_.loop !== "function") {
      console.log(`FAIL  level${nn}: 没有导出 loop()`);
      failed += 1;
      continue;
    }
    for (let t = 0; t < 3; t += 1) {
      module_.loop();
    }
    console.log(`PASS  level${nn}: 加载 ok，loop() x3 ok`);
  } catch (error) {
    console.log(`FAIL  level${nn}: ${error.message}`);
    failed += 1;
  }
}
console.log(`---------- ${10 - failed}/10 通过 ----------`);
process.exit(failed === 0 ? 0 : 1);
