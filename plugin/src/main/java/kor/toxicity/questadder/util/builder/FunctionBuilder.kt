package kor.toxicity.questadder.util.builder

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.*
import kor.toxicity.questadder.extension.storage
import kor.toxicity.questadder.extension.totalAmount
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.manager.ItemManager
import kor.toxicity.questadder.mechanic.npc.QuestNPC
import kor.toxicity.questadder.mechanic.quest.Quest
import kor.toxicity.questadder.mechanic.sender.ItemDialogSender
import kor.toxicity.questadder.util.HashedClass
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.function.ArgumentFunction
import kor.toxicity.questadder.util.function.QuestOperator
import kor.toxicity.questadder.util.function.WrappedFunction
import kor.toxicity.questadder.util.reflect.PrimitiveType
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat
import java.util.concurrent.ThreadLocalRandom
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

object FunctionBuilder {

    private val functionPattern = Pattern.compile("(?<name>\\w+)\\((?<argument>(\\w|\\W)*)\\)")
    private val stringPattern = Pattern.compile("^\'(?<string>(\\w|\'\'|\\W|^\')+)\'$")
    private val map = HashMap<HashedClass, MutableMap<String, MutableMap<HashedClassList, ArgumentFunction>>>()
    private val operatorMap = HashMap<HashedClass,MutableMap<String, StoredQuestOperator>>()
    private val booleanStringArray = setOf("true", "false")
    private val numberArray = arrayOf(
        '0','1','2','3','4','5','6','7','8','9'
    )
    private val allowedName = arrayOf(
        'a',
        'b',
        'c',
        'd',
        'e',
        'f',
        'g',
        'h',
        'i',
        'j',
        'k',
        'm',
        'n',
        'l',
        'o',
        'p',
        'q',
        'r',
        's',
        't',
        'u',
        'v',
        'w',
        'x',
        'y',
        'z',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'G',
        'H',
        'I',
        'J',
        'K',
        'M',
        'N',
        'L',
        'O',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'U',
        'V',
        'W',
        'X',
        'Y',
        'Z',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        '0',
        '§'
    )

    private val nullFunction = object : WrappedFunction {
        override fun getType(): Class<*> {
            return Null::class.java
        }

        override fun getReturnType(): Class<*> {
            return Null::class.java
        }

        override fun getName(): String {
            return "<none>"
        }

        override fun apply(t: Any): Any {
            return Null
        }

    }

    init {
        addOperation("==",0) { a: Any, b: Any ->
            a == b
        }
        addOperation("!=",0) { a: Any, b: Any ->
            a != b
        }
        addOperation("==",99) { a: Number, b: Number ->
            a.toDouble() == b.toDouble()
        }
        addOperation("!=",99) { a: Number, b: Number ->
            a.toDouble() != b.toDouble()
        }
        addOperation("===",0) { a: Any, b: Any ->
            a === b
        }
        addOperation("!==",0) { a: Any, b: Any ->
            a !== b
        }
        addOperation("+",99) { a: String, b: Any ->
            a + b
        }
        addOperation("+",99) { a: Number, b: Number ->
            a.toDouble() + b.toDouble()
        }
        addOperation("-",99) { a: Number, b: Number ->
            a.toDouble() - b.toDouble()
        }
        addOperation("/",99) { a: Number, b: Number ->
            a.toDouble() / b.toDouble()
        }
        addOperation("*",99) { a: Number, b: Number ->
            a.toDouble() * b.toDouble()
        }
        addOperation("%",99) { a: Number, b: Number ->
            a.toDouble() % b.toDouble()
        }
        addOperation(">",99) { a: Number, b: Number ->
            a.toDouble() > b.toDouble()
        }
        addOperation("<",99) { a: Number, b: Number ->
            a.toDouble() < b.toDouble()
        }
        addOperation(">=",99) { a: Number, b: Number ->
            a.toDouble() >= b.toDouble()
        }
        addOperation("<=",99) { a: Number, b: Number ->
            a.toDouble() <= b.toDouble()
        }


        addFunction("hello") { _: Null, _ ->
            "Hello world!"
        }
        addFunction("numOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            when (val n = QuestAdderBukkit.getPlayerData(args[0] as Player)?.get(args[1] as String)) {
                is Number -> n.toDouble()
                is String -> try {
                    n.toDouble()
                } catch (ex: Exception) {
                    0.0
                }
                else -> 0.0
            }
        }
        addFunction("strOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            QuestAdderBukkit.getPlayerData(args[0] as Player)?.get(args[1] as String).toString()
        }
        addFunction("boolOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            QuestAdderBukkit.getPlayerData(args[0] as Player)?.get(args[1] as String).toString().toBoolean()
        }
        addFunction("itemOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            QuestAdderBukkit.getPlayerData(args[0] as Player)?.get(args[1] as String) as? ItemStack
        }

        addFunction("questOf", listOf(String::class.java)) { _: Null, args ->
            DialogManager.getQuest(args[0] as String)
        }
        addFunction("has", listOf(Player::class.java, Quest::class.java)) { _: Null, args ->
            (args[1] as Quest).has(args[0] as Player)
        }
        addFunction("complete", listOf(Player::class.java, Quest::class.java)) { _: Null, args ->
            (args[1] as Quest).isCompleted(args[0] as Player)
        }
        addFunction("clear", listOf(Player::class.java, Quest::class.java)) { _: Null, args ->
            (args[1] as Quest).isCleared(args[0] as Player)
        }
        addFunction("ready", listOf(Player::class.java, Quest::class.java)) { _: Null, args ->
            (args[1] as Quest).isReady(args[0] as Player)
        }


        addFunction("random", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            ThreadLocalRandom.current().nextDouble((args[0] as Number).toDouble(), (args[1] as Number).toDouble())
        }
        addFunction("index", listOf(Player::class.java, QuestNPC::class.java)) { _: Null, args ->
            (args[1] as QuestNPC).getIndex(args[0] as Player) ?: 0
        }
        addFunction("npcOf", listOf(String::class.java)) { _: Null, args ->
            DialogManager.getQuestNPC(args[0] as String)
        }
        addFunction("player") { e: QuestAdderPlayerEvent, _ ->
            e.player
        }
        addFunction("quest") { e: QuestEvent, _ ->
            e.quest
        }
        addFunction("dialog") { e: DialogEvent, _ ->
            e.dialog
        }
        addFunction("npc") { e: NPCEvent, _ ->
            e.npc.toQuestNPC()
        }
        addFunction("location") { e: LocationEvent, _ ->
            e.namedLocation
        }
        addFunction("itemOf", listOf(String::class.java)) { _: Null, args ->
            ItemManager.getItem(args[0] as String)
        }
        addFunction("itemSenderOf", listOf(String::class.java)) { _: Null, args ->
            (DialogManager.getDialogSender(args[0] as String) as? ItemDialogSender)?.item
        }
        addFunction("args", listOf(Number::class.java)) { e: ActionInvokeEvent, args ->
            val index = (args[0] as Number).toInt()
            if (index >= 0 && index < e.args.size) e.args[index] else null
        }
        addFunction("var", listOf(String::class.java)) { e: QuestPlayerEvent, args ->
            QuestAdderBukkit.getPlayerData(e.player)?.getQuestVariable(e.quest.key,args[0] as String) ?: 0L
        }
        addFunction("exp") { e: QuestCompleteEvent, _ ->
            e.exp
        }
        addFunction("money") { e: QuestCompleteEvent, _ ->
            e.money
        }
        addFunction("name", listOf(Entity::class.java)) { _: Null, args ->
            (args[0] as Entity).name
        }
        addFunction("totalamount", listOf(Player::class.java,ItemStack::class.java)) { _: Null, args ->
            (args[0] as Player).totalAmount(args[1] as ItemStack)
        }
        addFunction("storage", listOf(Player::class.java,ItemStack::class.java)) { _: Null, args ->
            (args[0] as Player).storage(args[1] as ItemStack)
        }
        addFunction("weather", listOf(Player::class.java)) { _: Null, args ->
            val world = (args[0] as Player).world
            if (world.isThundering) "thunder"
            else if (world.hasStorm()) "storm"
            else "clear"
        }
        addFunction("sneak", listOf(Player::class.java)) { _: Null, args ->
            (args[0] as Player).isSneaking
        }
        addFunction("sprint", listOf(Player::class.java)) { _: Null, args ->
            (args[0] as Player).isSprinting
        }
        addFunction("health", listOf(Player::class.java)) { _: Null, args ->
            (args[0] as Player).health
        }
        addFunction("reversed", listOf(String::class.java)) { _: Null, args ->
            (args[0] as String).reversed()
        }
        addFunction("plus", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            (args[0] as Number).toDouble() + (args[1] as Number).toDouble()
        }
        addFunction("minus", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            (args[0] as Number).toDouble() - (args[1] as Number).toDouble()
        }
        addFunction("multiply", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            (args[0] as Number).toDouble() * (args[1] as Number).toDouble()
        }
        addFunction("divide", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            (args[0] as Number).toDouble() / (args[1] as Number).toDouble()
        }
        addFunction("mod", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            (args[0] as Number).toDouble() % (args[1] as Number).toDouble()
        }
        addFunction("comma", listOf(Number::class.java)) { _: Null, args ->
            DecimalFormat.getInstance().format((args[0] as Number).toDouble())
        }
        addFunction("ceil", listOf(Number::class.java)) { _: Null, args ->
            ceil((args[0] as Number).toDouble())
        }
        addFunction("floor", listOf(Number::class.java)) { _: Null, args ->
            floor((args[0] as Number).toDouble())
        }
        addFunction("round", listOf(Number::class.java)) { _: Null, args ->
            round((args[0] as Number).toDouble())
        }
        addFunction("and", listOf(Boolean::class.java, Boolean::class.java)) { _: Null, args ->
            args[0] as Boolean && args[1] as Boolean
        }
        addFunction("or", listOf(Boolean::class.java, Boolean::class.java)) { _: Null, args ->
            args[0] as Boolean || args[1] as Boolean
        }
        addFunction("region") { e: RegionEvent, _ ->
            e.region.id
        }
        addFunction("world", listOf(Player::class.java)) { _: Null, args ->
            (args[0] as Player).world.name
        }
        addFunction("papiStrOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            try {
                PlaceholderAPI.setPlaceholders(args[0] as Player, args[1] as String)
            } catch (ex: Throwable) {
                "<none>"
            }
        }
        addFunction("papiNumOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            try {
                PlaceholderAPI.setPlaceholders(args[0] as Player, args[1] as String).toInt()
            } catch (ex: Throwable) {
                0
            }
        }
        addFunction("papiBoolOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            try {
                PlaceholderAPI.setPlaceholders(args[0] as Player, args[1] as String).toBoolean()
            } catch (ex: Throwable) {
                false
            }
        }
    }
    inline fun <reified T, reified R : Any> addOperation(name: String, priority: Int, noinline operate: (T, T) -> R) {
        addOperation(name,priority,T::class.java,R::class.java,operate)
    }
    fun <T,R : Any> addOperation(name: String, priority: Int, clazz: Class<T>, returnType: Class<R>, operation: (T, T) -> R) {
        operatorMap.getOrPut(HashedClass(clazz)) {
            HashMap()
        }[name] = StoredQuestOperator(priority,object : QuestOperator {
            override fun getReturnType(): Class<*> {
                return returnType
            }

            override fun operate(a: Any, b: Any): Any {
                return operation(clazz.cast(a),clazz.cast(b))
            }

            override fun getName(): String {
                return name
            }
        })
    }


    inline fun <reified T : Any, reified R : Any> addFunction(
        name: String,
        args: List<Class<*>> = emptyList(),
        noinline function: (t: T, Array<Any>) -> R?
    ) {
        addFunction(name, T::class.java, R::class.java, args, function)
    }

    fun <T : Any, R: Any> addFunction(
        name: String,
        clazzType: Class<T>,
        returnType: Class<R>,
        args: List<Class<*>>,
        function: (t: T, Array<Any>) -> R?
    ) {
        map.getOrPut(HashedClass(returnType)) {
            HashMap()
        }.getOrPut(name) {
            HashMap()
        }.putIfAbsent(HashedClassList(args.map {
            HashedClass(it)
        }), object : ArgumentFunction {
            override fun getName(): String {
                return name
            }

            override fun getReturnType(): Class<*> {
                return returnType
            }

            override fun getArgumentType(): List<Class<*>> {
                return args
            }

            override fun apply(target: Any, args: Array<Any>): Any? {
                return if (clazzType.isAssignableFrom(target.javaClass)) {
                    function(clazzType.cast(target), args)
                } else if (Null::class.java.isAssignableFrom(clazzType)) {
                    function(clazzType.cast(Null), args)
                } else {
                    Null
                }
            }

            override fun getType(): Class<*> {
                return clazzType
            }
        })
    }

    private fun funcSplit(name: String): List<String> {
        val target = name.replace(" ","§")
        val list = ArrayList<String>()
        var i = 0
        val builder = StringBuilder()
        val operatorBuilder = StringBuilder()
        val length = target.length
        fun appendBuilder() {
            if (operatorBuilder.isNotEmpty()) {
                list.add(operatorBuilder.toString().replace("§",""))
                operatorBuilder.setLength(0)
            }
        }
        while (i < length) {
            val n = target[i]
            if (numberArray.contains(n)) {
                appendBuilder()
                var i2 = i
                while (length > i2 && numberArray.contains(target[i2])) {
                    builder.append(target[i2++])
                }
                list.add(builder.toString().replace("§"," "))
                builder.setLength(0)
                i = i2
            } else if (n == '\'') {
                appendBuilder()
                var i2 = i
                while (i2 < length) {
                    ++i2
                    when (target[i2]) {
                        '\\' -> {
                            i2++
                            continue
                        }
                        '\'' -> break
                    }
                }
                list.add(target.substring(i,++i2).replace("§"," ").replace("\\",""))
                i = i2
            } else if (n == '(') {
                var i3 = i + 1
                var first = i - 1
                var count = 0
                while (first >= 0 && allowedName.contains(target[first])) {
                    first--
                    count++
                }
                operatorBuilder.setLength(operatorBuilder.length - count)
                appendBuilder()
                var skip = false
                var sum = 1
                var sum2 = 0
                while (sum != sum2) {
                    when (target[i3++]) {
                        '\'' -> skip = !skip
                        '(' -> if (!skip) sum++
                        ')' -> if (!skip) sum2++
                    }
                }
                list.add(target.substring(first + 1, i3).replace("§"," "))
                i = i3
            }
            if (i < length) operatorBuilder.append(target[i++])
        }
        return if (list.isEmpty()) listOf(name) else list
    }

    private fun getPrimitiveFunction(string: String): WrappedFunction {
        val match = stringPattern.matcher(string)
        return if (match.find()) {
            val stringParsed = match.group("string").replace("\'\'", "\'")
            object : WrappedFunction {
                override fun getReturnType(): Class<*> {
                    return String::class.java
                }

                override fun getType(): Class<*> {
                    return Null::class.java
                }

                override fun getName(): String {
                    return stringParsed
                }

                override fun apply(t: Any): Any {
                    return stringParsed
                }
            }
        } else {
            val removeSpace = string.replace(" ","")
            if (booleanStringArray.contains(removeSpace)) {
                val booleanParsed = when (removeSpace) {
                    "true" -> true
                    else -> false
                }
                object : WrappedFunction {
                    override fun getType(): Class<*> {
                        return Null::class.java
                    }

                    override fun apply(t: Any): Any {
                        return booleanParsed
                    }

                    override fun getName(): String {
                        return booleanParsed.toString()
                    }

                    override fun getReturnType(): Class<*> {
                        return Boolean::class.java
                    }
                }
            } else try {
                val numberEquation = removeSpace.toDouble()
                object : WrappedFunction {
                    override fun getReturnType(): Class<*> {
                        return Number::class.java
                    }

                    override fun apply(t: Any): Any {
                        return numberEquation
                    }

                    override fun getName(): String {
                        return numberEquation.toString()
                    }

                    override fun getType(): Class<*> {
                        return Null::class.java
                    }
                }
            } catch (ex: Exception) {
                if (removeSpace != "null") QuestAdderBukkit.warn("compile error: unknown type: $removeSpace")
                nullFunction
            }
        }
    }

    fun evaluate(parameter: String, clazz: Class<*> = Any::class.java): WrappedFunction {
        if (parameter == "") return nullFunction
        fun functionMatch(clazz: Class<*>, target: List<String>): WrappedFunction? {
            fun find(clazz: Class<*>, target: String): WrappedFunction? {
                val matcher = functionPattern.matcher(target)
                return if (matcher.find()) {
                    val name = matcher.group("name")
                    val arg = matcher.group("argument")
                    val argument = if (arg == "") emptyList() else funcSplit(arg)


                    map.firstNotNullOfOrNull {
                        if (clazz.isAssignableFrom(it.key.clazz)) it.value[name]?.firstNotNullOfOrNull { e ->
                            val classes = e.key.classes.map {  c ->
                                c.clazz
                            }

                            val wrapped = if (classes.isEmpty()) emptyList() else ArrayList<WrappedFunction>().apply {
                                val split = ArrayList<List<String>>()
                                var i = 0
                                argument.forEachIndexed { index, s ->
                                    if (s == ",") {
                                        split.add(argument.subList(i,index))
                                        i = index + 1
                                    }
                                }
                                split.add(argument.subList(i,argument.size))
                                for (strings in split) {
                                    var find = find(Any::class.java,strings[0]) ?: continue

                                    if (strings.size > 1) {
                                        var i2 = 0
                                        while (i2 < strings.size - 2) {
                                            i2 += 2
                                            val before = find
                                            val after = find(Any::class.java,strings[i2]) ?: break
                                            findOperator(strings[i2 - 1],find.getReturnType())?.let { operator ->
                                                find = object : WrappedFunction {
                                                    override fun getReturnType(): Class<*> {
                                                        return operator.getReturnType()
                                                    }

                                                    override fun getType(): Class<*> {
                                                        return before.getType()
                                                    }

                                                    override fun getName(): String {
                                                        return operator.getName()
                                                    }

                                                    override fun apply(t: Any): Any {
                                                        val b = before.apply(t)
                                                        val a = after.apply(t)
                                                        return if (b != null && a != null) operator.operate(b,a) else Null
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    add(find)
                                }
                            }

                            var check = true
                            if (classes.size != wrapped.size) check = false
                            else {
                                val types = e.value.getArgumentType()
                                for ((i,type) in types.withIndex()) {
                                    if (!PrimitiveType.convertToReferenceClass(type).isAssignableFrom(wrapped[i].getReturnType())) {
                                        check = false
                                        break
                                    }
                                }
                            }
                            if (!check) {
                                null
                            } else {
                                val type = e.value.getType()
                                val ret = e.value.getReturnType()
                                val n = e.value.getName()
                                object : WrappedFunction {
                                    override fun getType(): Class<*> {
                                        return type
                                    }

                                    override fun getReturnType(): Class<*> {
                                        return ret
                                    }

                                    override fun getName(): String {
                                        return n
                                    }

                                    override fun apply(t: Any): Any? {
                                        return e.value.apply(t, wrapped.map { f ->
                                            f.apply(t) ?: Null
                                        }.toTypedArray())
                                    }
                              }
                            }
                        } else null
                    }
                } else getPrimitiveFunction(target)
            }
            return if (target.size == 1) {
                find(clazz,target[0])
            } else {
                var i = 0
                var ret: WrappedFunction? = find(clazz,target[0])
                while (i < target.lastIndex) {
                    ret?.let { func1 ->
                        findOperator(target[i+1],func1.getReturnType())?.let { operator ->
                            find(clazz, target[i + 2])?.let { func2 ->
                                ret = object : WrappedFunction {
                                    override fun getReturnType(): Class<*> {
                                        return operator.getReturnType()
                                    }

                                    override fun getType(): Class<*> {
                                        return clazz
                                    }

                                    override fun getName(): String {
                                        return operator.getName()
                                    }

                                    override fun apply(t: Any): Any {
                                        val f1 = func1.apply(t)
                                        val f2 = func2.apply(t)
                                        return if (f1 != null && f2 != null) operator.operate(f1,f2) else Null
                                    }
                                }
                            }
                        }
                    }
                    i += 2
                }
                ret
            }
        }
        return try {
            functionMatch(clazz, funcSplit(parameter))
        } catch (ex: Exception) {
            null
        } ?: nullFunction
    }
    private fun findOperator(name: String, clazz: Class<*>): QuestOperator? {
        val operatorList = ArrayList<StoredQuestOperator>()
        operatorMap.forEach {
            if (it.key.clazz.isAssignableFrom(clazz)) it.value.forEach { e ->
                if (e.key == name) operatorList.add(e.value)
            }
        }
        return operatorList.maxByOrNull {
            it.priority
        }?.operator
    }
    private class HashedClassList(val classes: List<HashedClass>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HashedClassList

            return classes == other.classes
        }

        override fun hashCode(): Int {
            return classes.hashCode()
        }
    }

    class StoredQuestOperator(val priority: Int, val operator: QuestOperator)
}
