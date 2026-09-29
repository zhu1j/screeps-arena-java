// Offline stand-in for the built-in "/game/visual" module.
//
// 形状必须和真机一致：模块导出的是一个 Visual 类。之前 mock 把画图函数挂在模块命名空间上，
// 恰好掩盖了绑定库一个真机必炸的 bug（visual.line is not a function）。
//
// 可视化离线没有画面，调用只是记进 world.logs，供测试断言"确实画了"。

import { world } from "./world.mjs";

export class Visual {
  constructor(layer = 0, persistent = false) {
    this.layer = layer;
    this.persistent = persistent;
  }

  line(from, to, style) {
    world.logs.push({ visual: "line", layer: this.layer, from, to, style });
    return this;
  }

  circle(pos, style) {
    world.logs.push({ visual: "circle", layer: this.layer, pos, style });
    return this;
  }

  text(text, pos, style) {
    world.logs.push({ visual: "text", layer: this.layer, text, pos, style });
    return this;
  }

  poly(points, style) {
    world.logs.push({ visual: "poly", layer: this.layer, points, style });
    return this;
  }

  rect(pos, w, h, style) {
    world.logs.push({ visual: "rect", layer: this.layer, pos, w, h, style });
    return this;
  }

  clear() {
    world.logs.push({ visual: "clear", layer: this.layer });
    return this;
  }

  size() {
    return 0;
  }
}
