// 一次跑完全部离线回归：三个 bot + 教程 10 关（有产物的话），
// 每一套都在"字符串 id"和"数字 id"两种模式各跑一遍。
//
//   node tools/tests/run.mjs [教程产物目录]
//
// 教程产物目录不传时依次试 TUTORIAL_DIR 环境变量、仓库自己的 dist/tutorial；
// 都找不到就跳过教程部分，只跑 bot。前提：先构建好产物（mvn package）。

import { spawnSync } from "node:child_process";
import { existsSync } from "node:fs";
import { resolve } from "node:path";

if (!existsSync("tools/register.mjs")) {
  console.error("请在仓库根目录运行：node tools/tests/run.mjs [教程产物目录]");
  process.exit(2);
}

const tutorialDir = process.argv[2] ?? process.env.TUTORIAL_DIR ?? resolve("dist/tutorial");
const hasTutorial = existsSync(`${tutorialDir}/level01/main.mjs`);

function run(script, args, numeric) {
  const label = `${script}${numeric ? "（数字 id）" : "（字符串 id）"}`;
  console.log(`\n===== ${label} =====`);
  const env = { ...process.env };
  if (numeric) {
    env.MOCK_NUMERIC_IDS = "1";
  } else {
    delete env.MOCK_NUMERIC_IDS;
  }
  const result = spawnSync(
    process.execPath,
    ["--import", "./tools/register.mjs", `tools/tests/${script}`, ...args],
    { stdio: "inherit", env }
  );
  if (result.error) {
    console.error(`${label} 起不来：${result.error.message}`);
    return false;
  }
  return result.status === 0;
}

const runs = [
  ["probe.mjs", [], false],
  ["probe.mjs", [], true],
  ["bots.mjs", [], false],
  ["bots.mjs", [], true],
];
if (hasTutorial) {
  runs.push(["smoke.mjs", [tutorialDir], false], ["smoke.mjs", [tutorialDir], true]);
  runs.push(["tutorial.mjs", [tutorialDir], false], ["tutorial.mjs", [tutorialDir], true]);
} else {
  console.log(`\n（跳过教程部分：找不到 ${tutorialDir}/level01/main.mjs，可传参或设 TUTORIAL_DIR）`);
}

const failed = [];
for (const [script, args, numeric] of runs) {
  if (!run(script, args, numeric)) {
    failed.push(`${script}${numeric ? "（数字 id）" : "（字符串 id）"}`);
  }
}

console.log("");
if (failed.length === 0) {
  console.log(`全部通过（${runs.length} 组）。`);
  process.exit(0);
}
console.log(`失败 ${failed.length}/${runs.length} 组：${failed.join("、")}`);
process.exit(1);
