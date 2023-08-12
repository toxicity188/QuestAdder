package kor.toxicity.questadder.util

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.EMPTY_DECORATION
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.deepClear
import kor.toxicity.questadder.util.builder.FunctionBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.regex.Pattern

class ComponentReader<T : Any>(string: String) {
    companion object {
        private val spaceFont = Key.key("questadder:space")

        private val emptyIterator = object : ComponentIterator {
            override fun nextLine(): Component {
                return Component.empty()
            }

            override fun hasNext(): Boolean {
                return false
            }
        }

        fun emptyIterator() = emptyIterator
        fun errorIterator() = object : ComponentIterator {

            private var index = 0

            override fun hasNext(): Boolean {
                return index < "error!".length
            }

            override fun nextLine(): Component {
                return "error!".substring(0,++index).asComponent().deepClear()
            }
        }

        private fun parseChar(int: Int): String {
            return if (int <= 0xFFFF) int.toChar().toString()
            else {
                val t = int - 0x10000
                "${((t ushr 10) + 0xD800).toChar()}${((t and 1023) + 0xDC00).toChar()}"
            }
        }

        private val pattern = Pattern.compile("<((?<name>([a-zA-Z]+)):(?<value>(\\w|,|_|-|#|:)+))>")

        private fun getNullBuilder(d: ComponentData) = StringComponentBuilder("<none>",d)
        private fun getPercentBuilder(d: ComponentData) = StringComponentBuilder("%",d)

        private val textColorMap = mapOf(
            "BLACK" to NamedTextColor.BLACK,
            "DARK_BLUE" to NamedTextColor.DARK_BLUE,
            "DARK_GREEN" to NamedTextColor.DARK_GREEN,
            "DARK_AQUA" to NamedTextColor.DARK_AQUA,
            "DARK_RED" to NamedTextColor.DARK_RED,
            "DARK_PURPLE" to NamedTextColor.DARK_PURPLE,
            "GOLD" to NamedTextColor.GOLD,
            "GRAY" to NamedTextColor.GRAY,
            "DARK_GRAY" to NamedTextColor.DARK_GRAY,
            "BLUE" to NamedTextColor.BLUE,
            "GREEN" to NamedTextColor.GREEN,
            "AQUA" to NamedTextColor.AQUA,
            "RED" to NamedTextColor.RED,
            "LIGHT_PURPLE" to NamedTextColor.LIGHT_PURPLE,
            "YELLOW" to NamedTextColor.YELLOW,
            "WHITE" to NamedTextColor.WHITE,
        )

        private val dataApply = mapOf<String,(String,ComponentData) -> ComponentBuilder?>(
            "font" to { s, c ->
                when (s) {
                    "null" -> c.font = null
                    else -> c.font = Key.key(s)
                }
                null
            },
            "color" to { s, c ->
                c.decoration = EMPTY_DECORATION
                if (s == "null") c.color = null
                else if (s.startsWith('#') && s.length == 7) c.color = TextColor.fromHexString(s.uppercase())
                else c.color = textColorMap[s.uppercase()]
                null
            },
            "decoration" to { s, c ->
                val map = TextDecoration.values().associateWith {
                    TextDecoration.State.FALSE
                }.toMutableMap()
                s.split(',').forEach {
                    try {
                        map[TextDecoration.valueOf(it.uppercase())] = TextDecoration.State.TRUE
                    } catch (ex: Exception) {
                        QuestAdder.warn("syntax error: cannot find the decoration '$it'")
                    }
                }
                c.decoration = map
                null
            },
            "space" to { s,_ ->
                try {
                    SpaceComponentBuilder(s.toInt())
                } catch (ex: Exception) {
                    QuestAdder.warn("number format error: the value \"$s\" is not an int.")
                    null
                }
            },
            "gradient" to { s, c ->
                val split = s.split('-')
                if (split.size == 2) {
                    fun getColor(str: String): TextColor? {
                        val case = str.uppercase()
                        return if (case.startsWith('#') && case.length == 7) TextColor.fromHexString(case.uppercase()) else textColorMap[case]
                    }
                    val color1 = getColor(split[0])
                    val color2 = getColor(split[1])
                    if (color1 != null && color2 != null) {
                        c.gradient = GradientData(color1,color2)
                    }
                }
                null
            }
        )
        fun parseString(s: String): List<String> {
            val list = ArrayList<String>()
            val arr = s.toCharArray()
            val sb = StringBuilder()
            for (c in arr) {
                when (c) {
                    '>' -> {
                        sb.append(c)
                        list.add(sb.toString())
                        sb.setLength(0)
                    }
                    '<' -> {
                        if (sb.isNotEmpty()) {
                            list.add(sb.toString())
                            sb.setLength(0)
                        }
                        sb.append(c)
                    }
                    else -> {
                        sb.append(c)
                    }
                }
            }
            if (sb.isNotEmpty()) list.add(sb.toString())
            return list
        }
    }

    private val components = ArrayList<(T) -> ComponentBuilder>()

    init {
        val comp = ComponentData()
        val split = string.replace('&','§').split('%')
        split.forEachIndexed { index, s ->
            if (index % 2 == 0) {
                if (index != 0 && index != split.lastIndex && s == "") components.add {
                    getPercentBuilder(comp)
                } else {
                    for (s1 in parseString(s)) {
                        val matcher = pattern.matcher(s1)
                        if (matcher.find()) {
                            dataApply[matcher.group("name")]?.let {
                                it(matcher.group("value"),comp)?.let { component ->
                                    components.add {
                                        component
                                    }
                                }
                            }
                        } else {
                            val copy = comp.copy()
                            comp.gradient?.let {
                                val builder = GradientComponentBuilder(s1,copy,it)
                                components.add {
                                    builder
                                }
                                comp.gradient = null
                            } ?: run {
                                val builder = StringComponentBuilder(s1,copy)
                                components.add {
                                    builder
                                }
                            }
                        }
                    }
                }
            }
            else {
                FunctionBuilder.evaluate(s).let { p ->
                    val copy = comp.copy()
                    components.add { t ->
                        p.apply(t)?.run {
                            StringComponentBuilder(toString(),copy)
                        } ?: getNullBuilder(copy)
                    }
                }
            }
        }
    }
    private interface ComponentBuilder {
        fun length(): Int
        fun build(index: Int): Component
        fun build(): Component
    }
    private class SpaceComponentBuilder(
        pixel: Int
    ): ComponentBuilder {
        private val component = parseChar(0xD0000 + pixel).asComponent().font(spaceFont)
        override fun length(): Int = 1
        override fun build(): Component = component
        override fun build(index: Int): Component = component
    }

    private class StringComponentBuilder(
        val string: String,
        val data: ComponentData
    ): ComponentBuilder {
        override fun length() = string.length
        override fun build(index: Int) = (if (string.isNotEmpty()) Component.text(string.substring(index, index + 1)) else Component.empty()).font(data.font).color(data.color).decorations(data.decoration)
        override fun build() = Component.text(string).font(data.font).color(data.color).decorations(data.decoration)
    }
    private class GradientComponentBuilder(
        val string: String,
        data: ComponentData,
        gradient: GradientData,
    ): ComponentBuilder {
        private val dataList = run {
            val from = gradient.from
            val to = gradient.to

            val fromRed = from.red()
            val fromGreen = from.green()
            val fromBlue = from.blue()

            val calRed = (to.red() - fromRed).toDouble()
            val calGreen = (to.green() - fromGreen).toDouble()
            val calBlue = (to.blue() - fromBlue).toDouble()

            Array(string.length) {
                val t = it.toDouble() / string.length.toDouble()
                string.substring(it,it + 1).asComponent().font(data.font).decorations(data.decoration).color(
                    TextColor.color(
                        fromRed + (calRed * t).toInt(),
                        fromGreen + (calGreen * t).toInt(),
                        fromBlue + (calBlue * t).toInt()
                    )
                )
            }
        }

        override fun length(): Int = dataList.size
        override fun build(): Component {
            var comp = Component.empty()
            for (component in dataList) {
                comp = comp.append(component)
            }
            return comp
        }

        override fun build(index: Int): Component = dataList[index]
    }

    private data class ComponentData(
        var font: Key? = null,
        var color: TextColor? = NamedTextColor.WHITE,
        var decoration: Map<TextDecoration,TextDecoration.State> = EMPTY_DECORATION,
        var gradient: GradientData? = null
    )
    private data class GradientData(
        val from: TextColor,
        val to: TextColor,
    )
    fun createIterator(t: T) = try {
        ComponentIteratorImpl(t)
    } catch (ex: Exception) {
        null
    }
    fun createComponent(t: T): Component? = try {
        var result: Component = Component.empty()
        components.forEach {
            result = result.append(it(t).build())
        }
        result
    } catch (ex: Exception) {
        null
    }


    interface ComponentIterator {
        fun hasNext(): Boolean
        fun nextLine(): Component
    }

    inner class ComponentIteratorImpl(private val t: T): ComponentIterator {
        private var index1 = 0
        private var index2 = 0

        private val parsedComponent = components.map {
            it(t)
        }

        override fun hasNext(): Boolean {
            return (index1 < parsedComponent.size && index2 < parsedComponent[index1].length())
        }

        override fun nextLine(): Component {
            val read = parsedComponent[index1]
            val ret = read.build(index2++)
            if (index2 == read.length()) {
                index2 = 0
                index1 ++
            }
            return ret
        }
    }
}