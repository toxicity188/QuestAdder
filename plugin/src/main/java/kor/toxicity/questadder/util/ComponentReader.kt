package kor.toxicity.questadder.util

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.deepClear
import kor.toxicity.questadder.util.function.FunctionBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.regex.Pattern

class ComponentReader<T : Any>(string: String) {
    companion object {

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


        private val pattern = Pattern.compile("<((?<name>([a-zA-Z]+)):(?<value>(\\w|,|#|:)+))>")

        private fun getNullBuilder(d: ComponentData) = ComponentBuilder("<none>",d)
        private fun getPercentBuilder(d: ComponentData) = ComponentBuilder("%",d)

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

        private val dataApply = mapOf<String,(String,ComponentData) -> Unit>(
            "font" to { s, c ->
                when (s) {
                    "null" -> c.font = null
                    else -> c.font = Key.key(s)
                }
            },
            "color" to { s, c ->
                if (s == "null") c.color = null
                else if (s.startsWith('#') && s.length == 7) c.color = TextColor.fromHexString(s.uppercase())
                else c.color = textColorMap[s.uppercase()]
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
        val split = string.split('%')
        split.forEachIndexed { index, s ->
            if (index % 2 == 0) {
                if (index != 0 && index != split.lastIndex && s == "") components.add {
                    getPercentBuilder(comp)
                } else {
                    for (s1 in parseString(s)) {
                        val matcher = pattern.matcher(s1)
                        if (matcher.find()) {
                            dataApply[matcher.group("name")]?.let {
                                it(matcher.group("value"),comp)
                            }
                        } else {
                            val copy = comp.copy()
                            components.add {
                                ComponentBuilder(s1,copy)
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
                            ComponentBuilder(toString(),copy)
                        } ?: getNullBuilder(copy)
                    }
                }
            }
        }
    }

    private class ComponentBuilder(
        val string: String,
        val data: ComponentData
    ) {
        fun build(index: Int) = Component.text(string.substring(index, index + 1)).font(data.font).color(data.color).decorations(data.decoration)
        fun build() = Component.text(string).font(data.font).color(data.color).decorations(data.decoration)
    }

    private data class ComponentData(
        var font: Key? = null,
        var color: TextColor? = NamedTextColor.WHITE,
        var decoration: Map<TextDecoration,TextDecoration.State> = mapOf(
            TextDecoration.OBFUSCATED to TextDecoration.State.FALSE,
            TextDecoration.BOLD to TextDecoration.State.FALSE,
            TextDecoration.UNDERLINED to TextDecoration.State.FALSE,
            TextDecoration.STRIKETHROUGH to TextDecoration.State.FALSE,
            TextDecoration.ITALIC to TextDecoration.State.FALSE,
        )
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
            return (index1 < parsedComponent.size && index2 < parsedComponent[index1].string.length)
        }

        override fun nextLine(): Component {
            val read = parsedComponent[index1]
            val ret = read.build(index2++)
            if (index2 == read.string.length) {
                index2 = 0
                index1 ++
            }
            return ret
        }
    }
}