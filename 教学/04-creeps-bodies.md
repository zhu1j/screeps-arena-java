# 第 4 关：Creeps bodies（Creep 的身体部件）

> 原文：客户端 `src/mocks/tutorial/4-creeps-bodies.template.html`（游戏内教程页面）。

你可以调用 creep 的各种函数，但不是每个 creep 都能用所有函数。一个 creep 能做什么动作，取决于它有哪些**身体部件**（body part）。可以查看它的 `body` 属性——一个数组，每个元素是某个类型的身体部件：

- MOVE —— 让 creep 能够移动
- ATTACK —— 允许它进行近战攻击
- RANGED_ATTACK —— 允许它攻击 3 格以内的目标
- HEAL —— 允许它治疗自己或其它 creep
- WORK —— 让工人 creep 可以建造或采集能量
- CARRY —— 提高 creep 携带资源的容量
- TOUGH —— 没有直接的主动效果

```javascript
if(creep.body.some(bodyPart => bodyPart.type == ATTACK)) {
    // 这个 creep 有 ATTACK 身体部件
}
```

每个身体部件给 creep 增加 100 点 `hits`。creep 受伤时身体部件也会受损，某个部件的 hits 归零后就不再起作用。同类型的部件越多，该类型能力的效果越强。

本关你有三种 creep，分别带 `ATTACK`、`RANGED_ATTACK` 和 `HEAL` 身体部件。只有让它们配合行动，才能击败对手。

做法和上一关一样，只是按身体部件区分职责：输出手用 `attack` 和 `rangedAttack` 攻击，治疗者用 `heal` 治疗受伤的己方 creep。

## 本关目标

击败敌方 creep。

## 官方示例代码（JavaScript）

```javascript
import { getObjectsByPrototype } from 'game/utils';
import { Creep } from 'game/prototypes';
import { ERR_NOT_IN_RANGE, ATTACK, RANGED_ATTACK, HEAL } from 'game/constants';

export function loop() {
    var myCreeps = getObjectsByPrototype(Creep).filter(creep => creep.my);
    var enemyCreep = getObjectsByPrototype(Creep).find(creep => !creep.my);

    for(var creep of myCreeps) {
        if(creep.body.some(bodyPart => bodyPart.type == ATTACK)) {
            if(creep.attack(enemyCreep) == ERR_NOT_IN_RANGE) {
                creep.moveTo(enemyCreep);
            }
        }
        if(creep.body.some(bodyPart => bodyPart.type == RANGED_ATTACK)) {
            if(creep.rangedAttack(enemyCreep) == ERR_NOT_IN_RANGE) {
                creep.moveTo(enemyCreep);
            }
        }
        if(creep.body.some(bodyPart => bodyPart.type == HEAL)) {
            var myDamagedCreeps = myCreeps.filter(i => i.hits < i.hitsMax);
            if(myDamagedCreeps.length > 0) {
                if(creep.heal(myDamagedCreeps[0]) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(myDamagedCreeps[0]);
                }
            }
        }
    }
}
```

（原页面此处有演示视频）

## Java 写法提示

```java
for (Creep creep : GameObjects.myCreeps()) {
    if (creep.hasBodyPart(BodyPartType.ATTACK)) {
        if (creep.attack(enemyCreep) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(enemyCreep);
        }
    }
    if (creep.hasBodyPart(BodyPartType.RANGED_ATTACK)) {
        if (creep.rangedAttack(enemyCreep) == ErrorCode.ERR_NOT_IN_RANGE) {
            creep.moveTo(enemyCreep);
        }
    }
    if (creep.hasBodyPart(BodyPartType.HEAL)) {
        for (Creep hurt : GameObjects.myCreeps()) {
            if (hurt.getHits() < hurt.getHitsMax()) {
                if (creep.heal(hurt) == ErrorCode.ERR_NOT_IN_RANGE) {
                    creep.moveTo(hurt);
                }
                break;
            }
        }
    }
}
```

- `creep.body.some(...)` → `creep.hasBodyPart(BodyPartType.ATTACK)`；也可以 `creep.getBody()` 拿到 `List<BodyPartStatus>`，再用 `status.getType()` 自己判断。
- `creep.hits` / `creep.hitsMax` → `creep.getHits()` / `creep.getHitsMax()`。
- `creep.heal(...)` 只接收 `Creep` 参数；`attack` / `rangedAttack` 接收任意 `GameObject`。

[← 第 3 关](03-first-attack.md) | [返回目录](README.md) | [第 5 关 →](05-store-and-transfer.md)
