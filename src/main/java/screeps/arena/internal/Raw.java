package screeps.arena.internal;

import org.teavm.jso.JSMethod;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * 竞技场内置 JS 模块的类型化视图。
 *
 * <p>这些接口不创建对象，只描述"那个活着的 JS 对象长什么样"。属性名写错会在编译期暴露；
 * 运行时它们就是原本的 JS 对象，没有任何包装。
 *
 * <p>命名规则：{@code @JSProperty("x")} 对应 {@code obj.x}，{@code @JSMethod("moveTo")}
 * 对应 {@code obj.moveTo(...)}。
 */
public final class Raw {

    private Raw() {
    }

    // ================================================================ 模块命名空间

    /** {@code /game/prototypes} —— 只用到里面的构造函数，用来给 {@code getObjectsByPrototype} 做筛选。 */
    public interface Prototypes extends JSObject {
        @JSProperty("GameObject")
        JSObject gameObject();

        @JSProperty("Creep")
        JSObject creep();

        @JSProperty("Structure")
        JSObject structure();

        @JSProperty("OwnedStructure")
        JSObject ownedStructure();

        @JSProperty("StructureSpawn")
        JSObject structureSpawn();

        @JSProperty("StructureTower")
        JSObject structureTower();

        @JSProperty("StructureExtension")
        JSObject structureExtension();

        @JSProperty("StructureContainer")
        JSObject structureContainer();

        @JSProperty("StructureRampart")
        JSObject structureRampart();

        @JSProperty("StructureWall")
        JSObject structureWall();

        @JSProperty("StructureRoad")
        JSObject structureRoad();

        @JSProperty("ConstructionSite")
        JSObject constructionSite();

        @JSProperty("Resource")
        JSObject resource();

        @JSProperty("Source")
        JSObject source();

        @JSProperty("Flag")
        JSObject flag();
    }

    /** {@code /game/constants} —— 数值与字符串常量。 */
    public interface Constants extends JSObject {
        @JSProperty("BODYPART_HITS")
        int bodyPartHits();

        @JSProperty("MAX_CREEP_SIZE")
        int maxCreepSize();

        @JSProperty("ATTACK_POWER")
        int attackPower();

        @JSProperty("RANGED_ATTACK_POWER")
        int rangedAttackPower();

        @JSProperty("HEAL_POWER")
        int healPower();

        @JSProperty("RANGED_HEAL_POWER")
        int rangedHealPower();

        @JSProperty("HARVEST_POWER")
        int harvestPower();

        @JSProperty("BUILD_POWER")
        int buildPower();

        @JSProperty("REPAIR_POWER")
        int repairPower();

        @JSProperty("DISMANTLE_POWER")
        int dismantlePower();

        @JSProperty("DISMANTLE_COST")
        double dismantleCost();

        @JSProperty("REPAIR_COST")
        double repairCost();

        @JSProperty("SOURCE_ENERGY_REGEN")
        int sourceEnergyRegen();

        @JSProperty("RESOURCE_DECAY")
        int resourceDecay();

        @JSProperty("TOWER_HITS")
        int towerHits();

        @JSProperty("TOWER_CAPACITY")
        int towerCapacity();

        @JSProperty("TOWER_ENERGY_COST")
        int towerEnergyCost();

        @JSProperty("TOWER_RANGE")
        int towerRange();

        @JSProperty("TOWER_COOLDOWN")
        int towerCooldown();

        @JSProperty("TOWER_OPTIMAL_RANGE")
        int towerOptimalRange();

        @JSProperty("TOWER_FALLOFF_RANGE")
        int towerFalloffRange();

        @JSProperty("TOWER_FALLOFF")
        double towerFalloff();

        @JSProperty("TOWER_POWER_ATTACK")
        int towerPowerAttack();

        @JSProperty("TOWER_POWER_HEAL")
        int towerPowerHeal();

        @JSProperty("TOWER_POWER_REPAIR")
        int towerPowerRepair();

        @JSProperty("SPAWN_HITS")
        int spawnHits();

        @JSProperty("SPAWN_ENERGY_CAPACITY")
        int spawnEnergyCapacity();

        @JSProperty("EXTENSION_HITS")
        int extensionHits();

        @JSProperty("EXTENSION_ENERGY_CAPACITY")
        int extensionEnergyCapacity();

        @JSProperty("CONTAINER_HITS")
        int containerHits();

        @JSProperty("CONTAINER_CAPACITY")
        int containerCapacity();

        @JSProperty("RAMPART_HITS")
        int rampartHits();

        @JSProperty("RAMPART_HITS_MAX")
        int rampartHitsMax();

        @JSProperty("WALL_HITS")
        int wallHits();

        @JSProperty("WALL_HITS_MAX")
        int wallHitsMax();

        @JSProperty("ROAD_HITS")
        int roadHits();

        @JSProperty("ROAD_WEAROUT")
        int roadWearout();

        @JSProperty("MAX_CONSTRUCTION_SITES")
        int maxConstructionSites();
    }

    /** {@code /game/utils} —— 全局工具函数。 */
    public interface Utils extends JSObject {
        @JSMethod("getTicks")
        int getTicks();

        @JSMethod("getCpuTime")
        double getCpuTime();

        @JSMethod("getObjects")
        JSObject getObjects();

        @JSMethod("getObjectsByPrototype")
        JSObject getObjectsByPrototype(JSObject prototype);

        @JSMethod("getObjectById")
        JSObject getObjectById(String id);

        @JSMethod("getRange")
        int getRange(JSObject a, JSObject b);

        @JSMethod("getDirection")
        JSObject getDirection(int dx, int dy);

        @JSMethod("getTerrainAt")
        JSObject getTerrainAt(JSObject position);

        @JSMethod("findInRange")
        JSObject findInRange(JSObject fromPosition, JSObject positions, int range);

        @JSMethod("findClosestByRange")
        JSObject findClosestByRange(JSObject fromPosition, JSObject positions);

        @JSMethod("findClosestByPath")
        JSObject findClosestByPath(JSObject fromPosition, JSObject positions);

        @JSMethod("findPath")
        JSObject findPath(JSObject fromPosition, JSObject toPosition);

        @JSMethod("findPath")
        JSObject findPath(JSObject fromPosition, JSObject toPosition, JSObject options);

        @JSMethod("createConstructionSite")
        JSObject createConstructionSite(JSObject position, JSObject structurePrototype);
    }

    /** {@code /game/path-finder}。 */
    public interface PathFinder extends JSObject {
        @JSMethod("searchPath")
        JSObject searchPath(JSObject origin, JSObject goal, JSObject options);

        @JSProperty("CostMatrix")
        JSObject costMatrix();
    }

    /**
     * {@code /game/visual} 的 {@code Visual} <b>实例</b>。
     *
     * <p>官方这个模块导出的是类，不是一组画图函数：先 {@code new Visual(layer, persistent)}
     * 建实例，再在实例上调 {@code line()/circle()/text()}。每个画图方法都返回实例本身，
     * 所以可以链式调用，见 {@link Js#newVisual()}。
     */
    public interface Visual extends JSObject {
        @JSProperty("layer")
        double layer();

        @JSProperty("persistent")
        boolean persistent();

        @JSMethod("line")
        Visual line(JSObject from, JSObject to, JSObject style);

        @JSMethod("circle")
        Visual circle(JSObject position, JSObject style);

        @JSMethod("text")
        Visual text(String text, JSObject position, JSObject style);

        @JSMethod("poly")
        Visual poly(JSObject points, JSObject style);

        @JSMethod("rect")
        Visual rect(JSObject position, double width, double height, JSObject style);

        @JSMethod("clear")
        Visual clear();

        @JSMethod("size")
        int size();
    }

    /**
     * {@code /arena} —— <b>不是每个关卡都有这个模块</b>，导出内容也随关卡变化。
     *
     * <p>基础关卡（tutorial、Spawn and Swamp）的官方模板里根本没有它，只有 Capture the
     * Flag 这类关卡才导得出 {@code BodyPart}。所以这个接口只能被关卡专属代码碰到：
     * 通用代码一旦引用它，产物里就会多一条 {@code import "/arena"}，而缺这个模块的关卡
     * 会在加载时整份脚本失败。见 {@code GameObjects.registerBodyPart()}。
     */
    public interface Arena extends JSObject {
        @JSProperty("BodyPart")
        JSObject bodyPart();
    }

    /** {@code /game} 根模块里的 {@code arenaInfo}。 */
    public interface ArenaInfo extends JSObject {
        @JSProperty("name")
        JSObject name();

        @JSProperty("level")
        int level();

        @JSProperty("season")
        JSObject season();

        @JSProperty("ticksLimit")
        int ticksLimit();

        @JSProperty("cpuTimeLimit")
        double cpuTimeLimit();

        @JSProperty("cpuTimeLimitFirstTick")
        double cpuTimeLimitFirstTick();
    }

    // ================================================================ 游戏对象

    /** 所有带坐标的对象。MOVE/ATTACK 这类接口的目标参数都收它。 */
    public interface Positioned extends JSObject {
        @JSProperty("x")
        int x();

        @JSProperty("y")
        int y();
    }

    public interface GameObject extends Positioned {
        /**
         * 对象的唯一标识。
         *
         * <p>官方 typings 写的是 {@code number|string}，真机也确实会给数字，所以这里保持原样，
         * 需要文本时用 {@link Js#stringOf} 转。声明成 {@code String} 会让 TeaVM 造出内容是数字的
         * 假字符串，之后一进哈希表就崩——原因见 {@link Js#stringOf}。
         */
        @JSProperty("id")
        JSObject id();

        @JSProperty("exists")
        boolean exists();

        /** 可选字段：有值时表示再过这么多 tick 对象就会消失；没有这个字段的对象不衰减。 */
        @JSProperty("ticksToDecay")
        JSObject ticksToDecay();

        @JSMethod("getRangeTo")
        int getRangeTo(JSObject target);

        @JSMethod("findPathTo")
        JSObject findPathTo(JSObject target);
    }

    public interface Creep extends GameObject {
        @JSProperty("my")
        boolean my();

        @JSProperty("hits")
        int hits();

        @JSProperty("hitsMax")
        int hitsMax();

        @JSProperty("fatigue")
        int fatigue();

        @JSProperty("body")
        JSObject body();

        @JSProperty("store")
        JSObject store();

        /** 孵化中的 creep 为 true，已经出场的为 false。 */
        @JSProperty("spawning")
        boolean spawning();

        @JSMethod("move")
        int move(int direction);

        @JSMethod("moveTo")
        int moveTo(JSObject target);

        @JSMethod("attack")
        int attack(JSObject target);

        @JSMethod("rangedAttack")
        int rangedAttack(JSObject target);

        @JSMethod("rangedMassAttack")
        int rangedMassAttack();

        @JSMethod("heal")
        int heal(JSObject target);

        @JSMethod("rangedHeal")
        int rangedHeal(JSObject target);

        @JSMethod("harvest")
        int harvest(JSObject target);

        @JSMethod("transfer")
        int transfer(JSObject target, String resourceType);

        @JSMethod("transfer")
        int transfer(JSObject target, String resourceType, int amount);

        @JSMethod("withdraw")
        int withdraw(JSObject target, String resourceType);

        @JSMethod("withdraw")
        int withdraw(JSObject target, String resourceType, int amount);

        @JSMethod("drop")
        int drop(String resourceType);

        @JSMethod("drop")
        int drop(String resourceType, int amount);

        @JSMethod("pickup")
        int pickup(JSObject target);

        @JSMethod("pull")
        int pull(JSObject target);

        @JSMethod("build")
        int build(JSObject target);
    }

    public interface BodyPartEntry extends JSObject {
        /** 官方类型是 string，统一走 {@link Js#stringOf} 读，理由见 {@link GameObject#id()}。 */
        @JSProperty("type")
        JSObject type();

        @JSProperty("hits")
        int hits();
    }

    public interface Structure extends GameObject {
        @JSProperty("hits")
        int hits();

        @JSProperty("hitsMax")
        int hitsMax();
    }

    public interface OwnedStructure extends Structure {
        @JSProperty("my")
        boolean my();
    }

    public interface StructureSpawn extends OwnedStructure {
        @JSProperty("store")
        JSObject store();

        @JSProperty("spawning")
        JSObject spawning();

        /** 新 creep 出场时允许移动的方向（{@code DirectionConstant} 数组）。 */
        @JSProperty("directions")
        JSObject directions();

        @JSMethod("spawnCreep")
        JSObject spawnCreep(JSObject body);

        @JSMethod("setDirections")
        int setDirections(JSObject directions);
    }

    public interface Spawning extends JSObject {
        @JSProperty("needTime")
        int needTime();

        @JSProperty("remainingTime")
        int remainingTime();

        @JSProperty("creep")
        JSObject creep();

        @JSMethod("cancel")
        int cancel();
    }

    public interface StructureTower extends OwnedStructure {
        @JSProperty("store")
        JSObject store();

        @JSProperty("cooldown")
        int cooldown();

        @JSMethod("attack")
        int attack(JSObject target);

        @JSMethod("heal")
        int heal(JSObject target);
    }

    public interface StructureContainer extends OwnedStructure {
        @JSProperty("store")
        JSObject store();
    }

    public interface StructureExtension extends OwnedStructure {
        @JSProperty("store")
        JSObject store();
    }

    public interface StructureRampart extends OwnedStructure {
    }

    public interface StructureWall extends Structure {
    }

    public interface StructureRoad extends Structure {
    }

    public interface ConstructionSite extends GameObject {
        @JSProperty("progress")
        int progress();

        @JSProperty("progressTotal")
        int progressTotal();

        /**
         * 官方 typings 没声明这个字段，但真机上有（客户端自己的界面就在读它）。
         * 统一走 {@link Js#stringOf} 读，读不到就是 {@code null}。
         */
        @JSProperty("structurePrototypeName")
        JSObject structurePrototypeName();

        @JSProperty("my")
        boolean my();

        @JSMethod("remove")
        void remove();
    }

    public interface Resource extends GameObject {
        @JSProperty("amount")
        int amount();

        /** 统一走 {@link Js#stringOf} 读，理由见 {@link GameObject#id()}。 */
        @JSProperty("resourceType")
        JSObject resourceType();
    }

    public interface Source extends GameObject {
        @JSProperty("energy")
        int energy();

        @JSProperty("energyCapacity")
        int energyCapacity();
    }

    /**
     * 中立旗子上根本没有 {@code my} 属性，所以这里不声明成 {@code boolean} 属性，
     * 由包装类用 {@link Js#booleanProperty} 按"缺省即 false"读取。
     */
    public interface Flag extends GameObject {
    }

    public interface ArenaBodyPart extends GameObject {
        @JSProperty("type")
        JSObject type();
    }

    /** {@code Store} 是纯数据对象，按资源名取数量。 */
    public interface Store extends JSObject {
        @JSMethod("getCapacity")
        JSObject getCapacity();

        @JSMethod("getCapacity")
        JSObject getCapacity(String resourceType);

        @JSMethod("getUsedCapacity")
        JSObject getUsedCapacity();

        @JSMethod("getUsedCapacity")
        JSObject getUsedCapacity(String resourceType);

        @JSMethod("getFreeCapacity")
        JSObject getFreeCapacity();

        @JSMethod("getFreeCapacity")
        JSObject getFreeCapacity(String resourceType);
    }

    public interface SpawnCreepResult extends JSObject {
        @JSProperty("object")
        JSObject object();

        @JSProperty("error")
        JSObject error();
    }

    public interface ConstructionSiteResult extends JSObject {
        @JSProperty("object")
        JSObject object();

        @JSProperty("error")
        JSObject error();
    }

    public interface SearchPathResult extends JSObject {
        @JSProperty("path")
        JSObject path();

        @JSProperty("ops")
        int ops();

        @JSProperty("cost")
        int cost();

        @JSProperty("incomplete")
        boolean incomplete();
    }

    public interface CostMatrixHandle extends JSObject {
        @JSMethod("set")
        void set(int x, int y, int cost);

        @JSMethod("get")
        int get(int x, int y);

        @JSMethod("clone")
        JSObject cloneMatrix();
    }
}
