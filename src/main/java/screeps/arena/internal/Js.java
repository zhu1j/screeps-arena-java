package screeps.arena.internal;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSBodyImport;
import org.teavm.jso.JSObject;

/**
 * 最低层的 JavaScript 互操作原语。
 *
 * <p>整个绑定库里只有这个类直接和 TeaVM 的 {@code @JSBody} 打交道。上层看到的是
 * {@link Raw} 里的类型化视图，再上层是 {@code screeps.arena} 里的包装类。
 *
 * <p>这里每个方法都对应一段内联进产物的 JavaScript。注意 {@code @JSBodyImport} 的
 * {@code alias} 是<b>模块命名空间在脚本里的名字</b>，所以脚本里要写 {@code utils.getTicks()}
 * 而不是 {@code getTicks()}。
 *
 * <p>反过来也要小心：{@code alias} 必须真的在 {@code script} 里出现。TeaVM 把这些别名绑定成
 * 脚本里的名字（最终指向模块导入），脚本里没写的名字就是自由变量，会被原样留在产物里变成一个
 * 未定义标识符——编译期不报错，只在真机跑起来的那一刻炸成 ReferenceError。想取模块上的导出，
 * 就老老实实写 {@code 模块别名.导出名}，别指望 TeaVM 认得裸的导出名。
 */
public final class Js {

    private Js() {
    }

    // ---------------------------------------------------------------- 模块导入

    @JSBody(
            params = {},
            script = "return prototypes;",
            imports = @JSBodyImport(alias = "prototypes", fromModule = "/game/prototypes"))
    public static native Raw.Prototypes prototypes();

    @JSBody(
            params = {},
            script = "return constants;",
            imports = @JSBodyImport(alias = "constants", fromModule = "/game/constants"))
    public static native Raw.Constants constants();

    @JSBody(
            params = {},
            script = "return utils;",
            imports = @JSBodyImport(alias = "utils", fromModule = "/game/utils"))
    public static native Raw.Utils utils();

    @JSBody(
            params = {},
            script = "return pathFinder;",
            imports = @JSBodyImport(alias = "pathFinder", fromModule = "/game/path-finder"))
    public static native Raw.PathFinder pathFinder();

    /**
     * 新建一个 {@code /game/visual} 的 {@code Visual} 实例。
     *
     * <p>真机的 visual 模块导出的是类（{@code new Visual(layer, persistent)}），而不是一组
     * 模块级函数：直接把命名空间当对象、调 {@code visual.line(...)} 会得到
     * {@code TypeError: visual.line is not a function}。所以这里必须是 {@code new}。
     */
    @JSBody(
            params = {},
            script = "return new visual.Visual();",
            imports = @JSBodyImport(alias = "visual", fromModule = "/game/visual"))
    public static native Raw.Visual newVisual();

    /** 同 {@link #newVisual()}，但指定图层。 */
    @JSBody(
            params = {"layer"},
            script = "return new visual.Visual(layer);",
            imports = @JSBodyImport(alias = "visual", fromModule = "/game/visual"))
    public static native Raw.Visual newVisual(double layer);

    /** 同 {@link #newVisual()}，但指定图层和是否跨 tick 保留。 */
    @JSBody(
            params = {"layer", "persistent"},
            script = "return new visual.Visual(layer, persistent);",
            imports = @JSBodyImport(alias = "visual", fromModule = "/game/visual"))
    public static native Raw.Visual newVisual(double layer, boolean persistent);

    /**
     * {@code /arena} 模块。
     *
     * <p>它<b>不是每个关卡都有</b>：产物里的 import 是按调用关系生成的，只要有一条路径能走到
     * 这里，每条产物都会带上 {@code import "/arena"}，而没有这个模块的关卡（tutorial、
     * Spawn and Swamp）一加载就整份脚本失败，表现是日志面板一片空白。所以别把它放进通用查询里，
     * 只让关卡专属代码用，见 {@code GameObjects.registerBodyPart()}。
     */
    @JSBody(
            params = {},
            script = "return arena;",
            imports = @JSBodyImport(alias = "arena", fromModule = "/arena"))
    public static native Raw.Arena arena();

    @JSBody(
            params = {},
            script = "return game.arenaInfo;",
            imports = @JSBodyImport(alias = "game", fromModule = "/game"))
    public static native Raw.ArenaInfo arenaInfo();

    // ---------------------------------------------------------------- 数组

    @JSBody(params = {"array"}, script = "return array.length;")
    public static native int length(JSObject array);

    @JSBody(params = {"array", "index"}, script = "return array[index];")
    public static native JSObject elementAt(JSObject array, int index);

    /** 往 JS 数组的指定下标写一个字符串。数组是自己的，可以用来自建传给游戏的参数。 */
    @JSBody(params = {"array", "index", "value"}, script = "array[index] = value;")
    public static native void setElement(JSObject array, int index, String value);

    /** 往 JS 数组的指定下标写一个数字。 */
    @JSBody(params = {"array", "index", "value"}, script = "array[index] = value;")
    public static native void setNumberElement(JSObject array, int index, int value);

    // ---------------------------------------------------------------- 值

    @JSBody(params = {"x", "y"}, script = "return {x: x, y: y};")
    public static native JSObject point(int x, int y);

    /**
     * 读取一个可能不存在的属性。
     *
     * <p>竞技场的返回值经常是 {@code {object}} 或 {@code {error}} 二选一，缺席的那个字段是
     * {@code undefined}。直接把它当 {@code int} 读会拿到无意义的值，所以凡是可选的数值都要
     * 先经过这里。
     */
    @JSBody(params = {"object", "key"}, script = "return object[key] === undefined ? null : object[key];")
    public static native JSObject optional(JSObject object, String key);

    @JSBody(params = {"value"}, script = "return value === undefined || value === null;")
    public static native boolean isNullish(JSObject value);

    @JSBody(params = {"value"}, script = "return value === undefined || value === null ? 0 : value;")
    public static native int numberOrZero(JSObject value);

    @JSBody(params = {"value"}, script = "return value === undefined || value === null ? 0 : value;")
    public static native double doubleOrZero(JSObject value);

    @JSBody(params = {"object", "key"}, script = "return object[key] === undefined ? 0 : object[key];")
    public static native int numberPropertyOrZero(JSObject object, String key);

    @JSBody(params = {"object", "key"}, script = "return object[key] === undefined;")
    public static native boolean isPropertyUndefined(JSObject object, String key);

    @JSBody(params = {"object", "key"}, script = "return object[key];")
    public static native JSObject property(JSObject object, String key);

    @JSBody(params = {"object"}, script = "return object === undefined ? null : object;")
    public static native JSObject orNull(JSObject value);

    @JSBody(params = {"value"}, script = "return value;")
    public static native boolean booleanValue(JSObject value);

    @JSBody(params = {"object", "prototype"}, script = "return object instanceof prototype;")
    public static native boolean isInstanceOf(JSObject object, JSObject prototype);

    /** 只把真正的 {@code true} 当成 true，{@code undefined} 和 0 都为 false。 */
    @JSBody(params = {"object", "key"}, script = "return object[key] === true;")
    public static native boolean booleanProperty(JSObject object, String key);

    // ---------------------------------------------------------------- 构造 JS 值

    @JSBody(params = {}, script = "return {};")
    public static native JSObject newObject();

    @JSBody(params = {}, script = "return [];")
    public static native JSObject newArray();

    @JSBody(params = {"array", "value"}, script = "array.push(value);")
    public static native void push(JSObject array, JSObject value);

    @JSBody(params = {"object", "key", "value"}, script = "object[key] = value;")
    public static native void setObject(JSObject object, String key, JSObject value);

    @JSBody(params = {"object", "key", "value"}, script = "object[key] = value;")
    public static native void setString(JSObject object, String key, String value);

    @JSBody(params = {"object", "key", "value"}, script = "object[key] = value;")
    public static native void setNumber(JSObject object, String key, double value);

    @JSBody(params = {"object", "key", "value"}, script = "object[key] = value;")
    public static native void setBoolean(JSObject object, String key, boolean value);

    /** 带范围的寻路目标：{@code {pos, range}}。 */
    @JSBody(params = {"pos", "range"}, script = "return {pos: pos, range: range};")
    public static native JSObject goal(JSObject pos, int range);

    @JSBody(
            params = {},
            script = "return new pathFinder.CostMatrix();",
            imports = @JSBodyImport(alias = "pathFinder", fromModule = "/game/path-finder"))
    public static native Raw.CostMatrixHandle newCostMatrix();

    // ---------------------------------------------------------------- 输出

    @JSBody(params = {"message"}, script = "console.log(message);")
    public static native void consoleLog(String message);

    // ---------------------------------------------------------------- 字符串

    /**
     * 把任意 JS 值读成 Java 字符串；值本身是 {@code undefined}/{@code null} 时返回 {@code null}。
     *
     * <p>必须走这里，不能把 JS 值直接当 Java 字符串用。TeaVM 给 {@code String} 类型的
     * JS 属性生成的转换是 {@code $rt_str(value)}，它只是把值塞进 Java 字符串的
     * {@code $nativeString} 字段，<b>完全不检查类型</b>。
     *
     * <p>而游戏里 {@code GameObject.id} 的官方类型是 {@code number|string}，真机会给数字：
     * 于是会做出一个"内容是数字的字符串"。这种假字符串一旦参与哈希——比如作为
     * {@code HashMap} 的 key——就会崩成
     * {@code TypeError: $this.$nativeString.charCodeAt is not a function}；
     * 值是 {@code undefined} 时崩成
     * {@code TypeError: Cannot read properties of undefined (reading 'length')}。
     *
     * <p>所以跨界时先让 JS 自己执行一次 {@code String(value)}，拿到货真价实的字符串再进 Java。
     * {@code undefined} 要保留"没有"这个含义：转成 {@code "undefined"} 这种字面量会让所有
     * 还没有 id 的对象（例如孵化中的 creep）挤进同一个缓存槽，互相认错。
     */
    public static String stringOf(JSObject value) {
        return isNullish(value) ? null : stringOfNonNull(value);
    }

    @JSBody(params = {"value"}, script = "return String(value);")
    private static native String stringOfNonNull(JSObject value);

    /** JS 的 {@code ===}：两个引用是不是同一个对象。 */
    @JSBody(params = {"a", "b"}, script = "return a === b;")
    public static native boolean sameObject(JSObject a, JSObject b);

    /**
     * 按 id 取对象：先按原样查，再退一步按数字查。
     *
     * <p>官方 typings 里 {@code getObjectById} 的参数写的是 {@code string}，但对象的 {@code id}
     * 实际是 {@code number|string}（见 {@link #stringOf}）。真机如果按 {@code ===} 比较，
     * 用字符串形式的数字 id 就查不到，所以这里再试一次数字形态。
     */
    @JSBody(
            params = {"id"},
            script = "var found = utils.getObjectById(id);"
                    + " if (found) { return found; }"
                    + " var numeric = Number(id);"
                    + " return Number.isFinite(numeric) ? (utils.getObjectById(numeric) || null) : null;",
            imports = @JSBodyImport(alias = "utils", fromModule = "/game/utils"))
    public static native JSObject objectById(String id);
}
