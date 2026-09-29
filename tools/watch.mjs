// 保存即编译：盯着 src/main/java，文件一变就重跑 TeaVM，直接写进客户端指向的那个目录。
//
//   node tools/watch.mjs                       # 编译 pom 里登记的所有关卡
//   node tools/watch.mjs spawn-and-swamp       # 只编译指定关卡
//
// 关卡清单和 TeaVM 版本都是从 pom.xml 里读的，加了新关卡不用改这里。
// mvn 需要额外的机器相关参数时（比如自定的本地仓库）走环境变量：
//
//   $env:MVN_ARGS='-Dmaven.repo.local=D:\maven\repository'
//
// 注意：这里跑的是 compile 阶段加单个 TeaVM 目标，不是完整的 mvn package——
// TeaVM 读的是 target/classes，所以编译阶段必须先跑，否则改了 Java 也没用。

import { spawn } from "node:child_process";
import { readFileSync, statSync, watch } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const projectDir = dirname(dirname(fileURLToPath(import.meta.url)));
const pom = readFileSync(join(projectDir, "pom.xml"), "utf8");

/** pom 里 teavm 插件登记了哪些关卡。 */
function arenasFromPom() {
    const plugin = pom.slice(pom.indexOf("teavm-maven-plugin"));
    const executions = plugin.slice(plugin.indexOf("<executions>"), plugin.indexOf("</executions>"));
    return [...executions.matchAll(/<id>([^<]+)<\/id>/g)].map((match) => match[1]);
}

const teavmVersion = pom.match(/<teavm\.version>([^<]+)<\/teavm\.version>/)[1];
const arenas = process.argv.slice(2).length > 0 ? process.argv.slice(2) : arenasFromPom();
const mvnArgs = (process.env.MVN_ARGS ?? "").split(/\s+/).filter(Boolean);
const watchDir = join(projectDir, "src", "main", "java");

let running = false;
let pending = false;
/** 上次开工的时刻：用来判断事件是不是构建期间冒出来的重复通知。 */
let lastBuildStartedAt = 0;

function build(reason) {
    if (running) {
        pending = true;
        return;
    }
    running = true;
    lastBuildStartedAt = Date.now();

    // compile 阶段负责把 .java 变成 target/classes，后面的目标才是 TeaVM 本体。
    const args = ["-B", "-ntp", ...mvnArgs, "compile",
        ...arenas.map((arena) => `org.teavm:teavm-maven-plugin:${teavmVersion}:compile@${arena}`)];

    console.log(`\n[watch] ${reason} → 编译 ${arenas.join(", ")}`);
    const startedAt = Date.now();
    const mvn = spawn(process.platform === "win32" ? "mvn.cmd" : "mvn", args, {
        cwd: projectDir,
        stdio: ["ignore", "pipe", "pipe"],
        shell: process.platform === "win32",
    });

    // 正常输出只在失败时才值得看，所以先攒着，成功了只留一行摘要。
    let output = "";
    mvn.stdout.on("data", (chunk) => { output += chunk; });
    mvn.stderr.on("data", (chunk) => { output += chunk; });

    mvn.on("close", (code) => {
        running = false;
        const seconds = ((Date.now() - startedAt) / 1000).toFixed(1);
        if (code === 0) {
            console.log(`[watch] 编译成功（${seconds}s）——游戏里要重开一局才会加载新代码`);
        } else {
            console.log(`[watch] 编译失败（${seconds}s）：`);
            console.log(output.trim());
        }
        if (pending) {
            pending = false;
            build("又检测到改动");
        }
    });
}

build("首次编译");

// 编辑器保存一个文件常常触发好几个事件，所以攒一小会儿再动手。
let debounce = null;
watch(watchDir, { recursive: true }, (_event, filename) => {
    if (!filename || !filename.endsWith(".java")) {
        return;
    }
    clearTimeout(debounce);
    debounce = setTimeout(() => {
        // Windows 上同一笔写入可能连着来两个事件，第二个只在文件真的又变过时才重新编译。
        try {
            if (statSync(join(watchDir, filename)).mtimeMs <= lastBuildStartedAt) {
                return;
            }
        } catch {
            return; // 文件已经不在了（改名/删除），等下一个事件
        }
        build(`改动 ${filename}`);
    }, 250);
});
