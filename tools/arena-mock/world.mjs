// Shared mutable state for the offline simulation. Both the mock game modules and the
// test runner reach into this, so it lives in its own module to avoid import cycles.

export const world = {
  /** Every live game object. */
  objects: [],
  /** Tick counter, advanced by the runner. */
  ticks: 0,
  nextId: 1,
  /** Console lines captured instead of printed, so tests can assert on them. */
  logs: [],

  newId(prefix) {
    // 真机上的 id 是 number|string，离线默认用字符串好读；
    // MOCK_NUMERIC_IDS=1 时切成数字，用来复现"绑定库把 id 当字符串用"的崩溃。
    if (process.env.MOCK_NUMERIC_IDS) {
      return this.nextId++;
    }
    return `${prefix}_${this.nextId++}`;
  },

  add(object) {
    this.objects.push(object);
    return object;
  },

  remove(object) {
    const index = this.objects.indexOf(object);
    if (index >= 0) {
      this.objects.splice(index, 1);
    }
    object.exists = false;
  },

  /** 推进一个 tick：先加时，再让所有对象结算自己的 tick 逻辑（孵化、冷却、回能）。 */
  advance() {
    this.ticks += 1;
    for (const object of [...this.objects]) {
      if (typeof object.tick === "function") {
        object.tick();
      }
    }
  },

  reset() {
    this.objects.length = 0;
    this.ticks = 0;
    this.nextId = 1;
    this.logs.length = 0;
  },
};
