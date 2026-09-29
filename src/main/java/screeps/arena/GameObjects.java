package screeps.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teavm.jso.JSObject;

import screeps.arena.internal.Js;
import screeps.arena.internal.Raw;

/**
 * 查找游戏对象的入口，对应官方的 getObjectsByPrototype / getObjects / getObjectById，
 * 但返回的是 Java 包装对象。
 *
 * <p>查到的对象按 id 缓存，因此同一个游戏对象在整场比赛里拿到的是同一个 Java 实例。
 * 少数对象（例如孵化中、还没拿到 id 的 creep）改用 JS 对象身份识别，规则一样。
 */
public final class GameObjects {

    private GameObjects() {
    }

    /** 把裸 JS 对象包成 Java 对象。 */
    @FunctionalInterface
    interface Wrapper {
        GameObject wrap(JSObject raw);
    }

    /** 一个游戏原型：Java 类、对应的 JS 构造函数、以及包装方式。 */
    private static final class Entry {
        final Class<?> type;
        final JSObject prototype;
        final Wrapper wrapper;

        Entry(Class<?> type, JSObject prototype, Wrapper wrapper) {
            this.type = type;
            this.prototype = prototype;
            this.wrapper = wrapper;
        }
    }

    /** 顺序有意义：派生类型必须排在基类前面，否则识别出来的类型会过于笼统。 */
    private static List<Entry> entries;

    /**
     * 关卡专属原型（来自 {@code /arena}），默认是空的。
     *
     * <p>它们比 {@link #entries()} 里的任何一条都具体，所以查找和识别都要排在前面。
     */
    private static final List<Entry> ARENA_ENTRIES = new ArrayList<>();

    private static final Map<String, GameObject> CACHE = new HashMap<>();

    /**
     * 还没有 id 的对象（例如刚下单、还在孵化里的 creep）。
     *
     * <p>它们没法用 id 当缓存键；如果硬塞进 {@link #CACHE}，所有没 id 的对象都会落在同一个
     * 键上、互相认错。所以这里按 JS 对象身份（{@code ===}）去找。
     */
    private static final List<GameObject> UNIDENTIFIED = new ArrayList<>();

    private static List<Entry> entries() {
        if (entries == null) {
            entries = buildEntries();
        }
        return entries;
    }

    private static List<Entry> buildEntries() {
        Raw.Prototypes prototypes = Js.prototypes();
        List<Entry> list = new ArrayList<>();
        list.add(new Entry(Creep.class, prototypes.creep(), raw -> new Creep(raw)));
        list.add(new Entry(StructureSpawn.class, prototypes.structureSpawn(), raw -> new StructureSpawn(raw)));
        list.add(new Entry(StructureTower.class, prototypes.structureTower(), raw -> new StructureTower(raw)));
        list.add(new Entry(StructureExtension.class, prototypes.structureExtension(), raw -> new StructureExtension(raw)));
        list.add(new Entry(StructureContainer.class, prototypes.structureContainer(), raw -> new StructureContainer(raw)));
        list.add(new Entry(StructureRampart.class, prototypes.structureRampart(), raw -> new StructureRampart(raw)));
        list.add(new Entry(StructureWall.class, prototypes.structureWall(), raw -> new StructureWall(raw)));
        list.add(new Entry(StructureRoad.class, prototypes.structureRoad(), raw -> new StructureRoad(raw)));
        list.add(new Entry(OwnedStructure.class, prototypes.ownedStructure(), raw -> new OwnedStructure(raw)));
        list.add(new Entry(ConstructionSite.class, prototypes.constructionSite(), raw -> new ConstructionSite(raw)));
        list.add(new Entry(Resource.class, prototypes.resource(), raw -> new Resource(raw)));
        list.add(new Entry(Source.class, prototypes.source(), raw -> new Source(raw)));
        list.add(new Entry(Structure.class, prototypes.structure(), raw -> new Structure(raw)));

        // Flag 是 /game/prototypes 的导出，每个关卡的官方 typings 都带它。别再回 "/arena" 拿：
        // 那个模块只有部分关卡存在，引了它会让别的关卡连加载都失败。
        addIfPresent(list, Flag.class, prototypes.flag(), raw -> new Flag(raw));

        list.add(new Entry(GameObject.class, prototypes.gameObject(), GameObject::new));
        return list;
    }

    /**
     * 登记 {@code /arena} 里的 {@link BodyPart} 原型；登记之后 {@code of(BodyPart.class)}
     * 才能用。重复调用没有副作用。
     *
     * <p>为什么要手动登记：{@code /arena} 不是每个关卡都有的模块（tutorial、Spawn and Swamp
     * 就没有），而产物里的 {@code import} 是按调用关系生成的——只要有一条路径能走到读它的代码，
     * 每条产物都会带上 {@code import "/arena"}，那些关卡一加载就整份脚本失败。做成显式登记，
     * "引不引这个模块"就由关卡代码自己决定，会掉身体部件的关卡才需要调它。
     */
    public static void registerBodyPart() {
        for (Entry entry : ARENA_ENTRIES) {
            if (entry.type == BodyPart.class) {
                return;
            }
        }
        addIfPresent(ARENA_ENTRIES, BodyPart.class, Js.arena().bodyPart(), raw -> new BodyPart(raw));
    }

    private static void addIfPresent(List<Entry> list, Class<?> type, JSObject prototype, Wrapper wrapper) {
        if (!Js.isNullish(prototype)) {
            list.add(new Entry(type, prototype, wrapper));
        }
    }

    /** 把 {@link StructureType} 翻译成游戏里的原型构造函数。 */
    static JSObject prototypeFor(StructureType type) {
        Raw.Prototypes prototypes = Js.prototypes();
        switch (type) {
            case TOWER:
                return prototypes.structureTower();
            case EXTENSION:
                return prototypes.structureExtension();
            case ROAD:
                return prototypes.structureRoad();
            case CONTAINER:
                return prototypes.structureContainer();
            case WALL:
                return prototypes.structureWall();
            case RAMPART:
                return prototypes.structureRampart();
            case SPAWN:
                return prototypes.structureSpawn();
            default:
                throw new IllegalArgumentException("没有登记的建筑种类：" + type);
        }
    }

    private static JSObject prototypeOf(Class<?> type) {
        for (Entry entry : ARENA_ENTRIES) {
            if (entry.type == type) {
                return entry.prototype;
            }
        }
        for (Entry entry : entries()) {
            if (entry.type == type) {
                return entry.prototype;
            }
        }
        throw new IllegalArgumentException("当前关卡里没有这个原型，或者还没登记：" + type.getName());
    }

    // ---------------------------------------------------------------- 常用查询

    public static List<Creep> creeps() {
        return of(Creep.class);
    }

    public static List<Creep> myCreeps() {
        List<Creep> result = new ArrayList<>();
        for (Creep creep : creeps()) {
            if (creep.isMine()) {
                result.add(creep);
            }
        }
        return result;
    }

    public static List<Creep> enemyCreeps() {
        List<Creep> result = new ArrayList<>();
        for (Creep creep : creeps()) {
            if (!creep.isMine()) {
                result.add(creep);
            }
        }
        return result;
    }

    public static List<StructureSpawn> spawns() {
        return of(StructureSpawn.class);
    }

    /** 我的 spawn；一个都没有时返回 {@code null}。 */
    public static StructureSpawn mySpawn() {
        for (StructureSpawn spawn : spawns()) {
            if (spawn.isMine()) {
                return spawn;
            }
        }
        return null;
    }

    /** 敌方的 spawn；一个都没有时返回 {@code null}。 */
    public static StructureSpawn enemySpawn() {
        for (StructureSpawn spawn : spawns()) {
            if (!spawn.isMine()) {
                return spawn;
            }
        }
        return null;
    }

    /** 场上的旗子。没有旗子的关卡返回空列表。 */
    public static List<Flag> flags() {
        return of(Flag.class);
    }

    public static List<Source> sources() {
        return of(Source.class);
    }

    /** 取回某种原型的全部对象，例如 GameObjects.of(Creep.class) 就是所有 creep。 */
    @SuppressWarnings("unchecked")
    public static <T extends GameObject> List<T> of(Class<T> type) {
        JSObject array = Js.utils().getObjectsByPrototype(prototypeOf(type));
        int size = Js.length(array);
        List<T> result = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            result.add((T) wrap(Js.elementAt(array, index)));
        }
        return result;
    }

    /** 对局里当前存在的全部对象。顺序不保证。 */
    public static List<GameObject> all() {
        JSObject array = Js.utils().getObjects();
        int size = Js.length(array);
        List<GameObject> result = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            result.add(wrap(Js.elementAt(array, index)));
        }
        return result;
    }

    /** 按 id 取对象；对象已经不在了返回 null。 */
    public static GameObject byId(String id) {
        if (id == null) {
            return null;
        }
        JSObject found = Js.orNull(Js.objectById(id));
        return found == null ? null : wrap(found);
    }

    /** 把裸 JS 对象包成 Java 对象，同一个对象复用同一个实例。 */
    public static GameObject wrap(JSObject raw) {
        Raw.GameObject object = (Raw.GameObject) raw;
        // id 在真机上是 number|string，而且孵化中的对象可能还没有 id，见 Js#stringOf。
        String id = Js.stringOf(object.id());
        if (id != null) {
            GameObject cached = CACHE.get(id);
            if (cached != null) {
                return cached;
            }
            // 之前可能以"还没有 id"的身份登记过；现在有 id 了就转正，不再新建实例。
            GameObject created = takeUnidentified(object);
            if (created == null) {
                created = detect(object);
            }
            CACHE.put(id, created);
            return created;
        }

        GameObject cached = findUnidentified(object);
        if (cached != null) {
            return cached;
        }
        GameObject created = detect(object);
        UNIDENTIFIED.add(created);
        return created;
    }

    private static GameObject findUnidentified(Raw.GameObject object) {
        for (int index = 0; index < UNIDENTIFIED.size(); index++) {
            GameObject candidate = UNIDENTIFIED.get(index);
            if (Js.sameObject(candidate.raw(), object)) {
                return candidate;
            }
        }
        return null;
    }

    private static GameObject takeUnidentified(Raw.GameObject object) {
        GameObject found = findUnidentified(object);
        if (found != null) {
            UNIDENTIFIED.remove(found);
        }
        return found;
    }

    /** 清空包装对象缓存。只在换对局时调用，比赛进行中不要调。 */
    public static void clearCache() {
        CACHE.clear();
        UNIDENTIFIED.clear();
    }

    private static GameObject detect(Raw.GameObject object) {
        for (Entry entry : ARENA_ENTRIES) {
            if (Js.isInstanceOf(object, entry.prototype)) {
                return entry.wrapper.wrap(object);
            }
        }
        for (Entry entry : entries()) {
            if (Js.isInstanceOf(object, entry.prototype)) {
                return entry.wrapper.wrap(object);
            }
        }
        return new GameObject(object);
    }

    /** 把 Java 侧的"位置"转换成传给游戏函数的 JS 对象。 */
    public static JSObject toRaw(Position position) {
        if (position instanceof GameObject object) {
            return object.raw();
        }
        return Js.point(position.getX(), position.getY());
    }

    static List<Point> toPoints(JSObject path) {
        List<Point> result = new ArrayList<>();
        if (path == null) {
            return result;
        }
        int size = Js.length(path);
        for (int index = 0; index < size; index++) {
            Raw.Positioned step = (Raw.Positioned) Js.elementAt(path, index);
            result.add(Point.of(step.x(), step.y()));
        }
        return result;
    }
}
