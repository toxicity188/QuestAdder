package kor.toxicity.questadder.util.builder

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.ActionEvent
import kor.toxicity.questadder.event.ArgumentEvent
import kor.toxicity.questadder.event.DialogEvent
import kor.toxicity.questadder.event.NPCEvent
import kor.toxicity.questadder.event.QuestAdderPlayerEvent
import kor.toxicity.questadder.event.QuestEvent
import kor.toxicity.questadder.event.QuestPlayerEvent
import kor.toxicity.questadder.mechanic.Quest
import kor.toxicity.questadder.util.HashedClass
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.function.ArgumentFunction
import kor.toxicity.questadder.util.function.QuestOperator
import kor.toxicity.questadder.util.function.WrappedFunction
import kor.toxicity.questadder.util.reflect.PrimitiveType
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
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
    private val operatorMap = HashMap<HashedClass,MutableMap<String, QuestOperator>>()
    private val booleanStringArray = arrayOf("true", "false")
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
        addOperation("==") { a: Any, b: Any ->
            a == b
        }
        addOperation("!=") { a: Any, b: Any ->
            a != b
        }
        addOperation("===") { a: Any, b: Any ->
            a === b
        }
        addOperation("!==") { a: Any, b: Any ->
            a !== b
        }
        addOperation("+") { a: String, b: Any ->
            a + b
        }
        addOperation("+") { a: Number, b: Number ->
            a.toDouble() + b.toDouble()
        }
        addOperation("-") { a: Number, b: Number ->
            a.toDouble() - b.toDouble()
        }
        addOperation("/") { a: Number, b: Number ->
            a.toDouble() / b.toDouble()
        }
        addOperation("*") { a: Number, b: Number ->
            a.toDouble() * b.toDouble()
        }
        addOperation("%") { a: Number, b: Number ->
            a.toDouble() % b.toDouble()
        }
        addOperation(">") { a: Number, b: Number ->
            a.toDouble() > b.toDouble()
        }
        addOperation("<") { a: Number, b: Number ->
            a.toDouble() < b.toDouble()
        }
        addOperation(">=") { a: Number, b: Number ->
            a.toDouble() > b.toDouble()
        }
        addOperation("<=") { a: Number, b: Number ->
            a.toDouble() < b.toDouble()
        }


        addFunction("hello") { _: Null, _ ->
            "Hello world!"
        }
        addFunction("numOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            when (val n = QuestAdder.getPlayerData(args[0] as Player)?.get(args[1] as String)) {
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
            QuestAdder.getPlayerData(args[0] as Player)?.get(args[1] as String).toString()
        }
        addFunction("boolOf", listOf(Player::class.java, String::class.java)) { _: Null, args ->
            QuestAdder.getPlayerData(args[0] as Player)?.get(args[1] as String).toString().toBoolean()
        }
        addFunction("random", listOf(Number::class.java, Number::class.java)) { _: Null, args ->
            ThreadLocalRandom.current().nextDouble((args[0] as Number).toDouble(), (args[1] as Number).toDouble())
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
            e.npc
        }
        addFunction("args", listOf(Number::class.java)) { e: ArgumentEvent, args ->
            val index = (args[0] as Number).toInt()
            if (index >= 0 && index < e.args.size) e.args[index] else null
        }
        addFunction("var", listOf(String::class.java)) { e: QuestPlayerEvent, args ->
            QuestAdder.getPlayerData(e.player)?.getQuestVariable(e.quest.key,args[0] as String) ?: 0
        }
        addFunction("name", listOf(Entity::class.java)) { _: Null, args ->
            (args[0] as Entity).name
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
    }
    inline fun <reified T, reified R : Any> addOperation(name: String, noinline operate: (T, T) -> R) {
        addOperation(name,T::class.java,R::class.java,operate)
    }
    fun <T,R : Any> addOperation(name: String, clazz: Class<T>, returnType: Class<R>, operation: (T, T) -> R) {
        operatorMap.getOrPut(HashedClass(clazz)) {
            HashMap()
        }[name] = object : QuestOperator {
            override fun getReturnType(): Class<*> {
                return returnType
            }

            override fun operate(a: Any?, b: Any?): Any {
                return operation(clazz.cast(a),clazz.cast(b))
            }

            override fun getName(): String {
                return name
            }
        }
    }


    inline fun <reified T, reified R> addFunction(
        name: String,
        args: List<Class<*>> = emptyList(),
        noinline function: (t: T, Array<Any>) -> R?
    ) {
        addFunction(name, T::class.java, R::class.java, args, function)
    }

    fun <T, R> addFunction(
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
        } else if (booleanStringArray.contains(string)) {
            val booleanParsed = when (string) {
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
            val numberEquation = string.toDouble()
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
            if (string != "null") QuestAdder.warn("compile error: unknown type: $string")
            nullFunction
        }
    }

    fun evaluate(parameter: String, clazz: Class<*> = Any::class.java): WrappedFunction {
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
                                                        return operator.operate(before.apply(t),after.apply(t))
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
                                        return operator.operate(func1.apply(t),func2.apply(t))
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
    private fun findOperator(name: String, clazz: Class<*>) = operatorMap.firstNotNullOfOrNull {
        if (it.key.clazz.isAssignableFrom(clazz)) it.value.firstNotNullOfOrNull { e ->
            if (e.key == name) e.value else null
        } else null
    }
    private class HashedClassList(val classes: List<HashedClass>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HashedClassList

            if (classes != other.classes) return false

            return true
        }

        override fun hashCode(): Int {
            return classes.hashCode()
        }
    }
}