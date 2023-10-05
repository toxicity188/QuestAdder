package kor.toxicity.questadder.util

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.EMPTY_DECORATION
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.deepClear
import kor.toxicity.questadder.extension.parseChar
import kor.toxicity.questadder.manager.ResourcePackManager
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
        private val buildFont = Key.key("questadder:build")
        private val legacySpaceFont = Key.key("questadder:legacy_space")

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

        fun splitFunction(string: String): List<String> {
            var index = 0
            var length = 0
            val list = ArrayList<String>()
            while (length < string.length) {
                if (string.substring(length,length + 1) == "%") {
                    if (length < string.lastIndex && string.substring(length + 1, length + 2) == "%") {
                        length ++
                    } else {
                        list.add(string.substring(index,length).replace("%%","%"))
                        index = length + 1
                    }
                }
                length++
            }
            list.add(string.substring(index).replace("%%","%"))
            return list
        }


        private val pattern = Pattern.compile("<((?<name>([a-zA-Z]+)):(?<value>(\\w|,|_|-|#|:)+))>")

        private fun getNullBuilder(d: ComponentData) = StringComponentBuilder("<none>",d, emptyMap())

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
                val map = TextDecoration.entries.associateWith {
                    TextDecoration.State.FALSE
                }.toMutableMap()
                s.split(',').forEach {
                    try {
                        map[TextDecoration.valueOf(it.uppercase())] = TextDecoration.State.TRUE
                    } catch (ex: Exception) {
                        QuestAdderBukkit.warn("syntax error: cannot find the decoration '$it'")
                    }
                }
                c.decoration = map
                null
            },
            "space" to { s,_ ->
                try {
                    if (QuestAdderBukkit.nms.getVersion().version >= 19) SpaceComponentBuilder(s.toInt()) else LegacySpaceComponentBuilder(s.toInt())
                } catch (ex: Exception) {
                    QuestAdderBukkit.warn("number format error: the value \"$s\" is not an int.")
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
            },
            "image" to { s, c ->
                ResourcePackManager.getImageFont(s)?.let {
                    ImageComponentBuilder(it,c)
                }
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
        private fun parse(original: String, pair: Map<String, List<Component>>): List<Component> {
            return when (pair.size) {
                0 -> original.toCharArray().map {
                    Component.text(it)
                }
                else -> {
                    val list = ArrayList<Component>()
                    fun split(target: String, map: Map<String, List<Component>>) {
                        when (map.size) {
                            0 -> {
                                //never reach this
                            }
                            1 -> {
                                val entry = map.entries.single()
                                val split = target.split(entry.key)
                                split.forEachIndexed { index, s ->
                                    list.addAll(parse(s, emptyMap()))
                                    if (index < split.lastIndex) list.addAll(entry.value)
                                }
                            }
                            else -> {
                                val entry = map.entries.first()
                                val newMap = HashMap(map).apply {
                                    remove(entry.key)
                                }
                                val split = target.split(entry.key)
                                split.forEachIndexed { index, s ->
                                    split(s, newMap)
                                    if (index < split.lastIndex) list.addAll(entry.value)
                                }
                            }
                        }
                    }
                    split(original, pair)
                    list
                }
            }
        }
    }

    private val components = ArrayList<(T, Map<String, List<Component>>) -> ComponentBuilder>()

    init {
        val comp = ComponentData()
        val split = splitFunction(string.replace('&','§'))
        split.forEachIndexed { index, s ->
            if (s == "") return@forEachIndexed
            if (index % 2 == 0) {
                for (s1 in parseString(s)) {
                    val matcher = pattern.matcher(s1)
                    if (matcher.find()) {
                        dataApply[matcher.group("name")]?.let {
                            it(matcher.group("value"),comp)?.let { component ->
                                components.add { _, _ ->
                                    component
                                }
                            }
                        }
                    } else {
                        val copy = comp.copy()
                        comp.gradient?.let {
                            components.add { _, m ->
                                GradientComponentBuilder(s1,copy,it,m)
                            }
                            comp.gradient = null
                        } ?: run {
                            components.add { _, m ->
                                StringComponentBuilder(s1,copy,m)
                            }
                        }
                    }
                }
            } else {
                FunctionBuilder.evaluate(s).let { p ->
                    val copy = comp.copy()
                    components.add { t, m ->
                        p.apply(t)?.run {
                            StringComponentBuilder(toString(),copy,m)
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
        private val component = (0xD0000 + pixel).parseChar().asComponent().font(spaceFont)
        override fun length(): Int = 1
        override fun build(): Component = component
        override fun build(index: Int): Component = component
    }
    private class LegacySpaceComponentBuilder(
        pixel: Int
    ): ComponentBuilder {
        private val component = (0xFFC00 + pixel).parseChar().asComponent().font(legacySpaceFont)
        override fun length(): Int = 1
        override fun build(): Component = component
        override fun build(index: Int): Component = component
    }
    private class ImageComponentBuilder(string: String, data: ComponentData): ComponentBuilder {
        private val component = string.asComponent().color(data.color).decorations(data.decoration).font(buildFont)
        override fun length(): Int = 1
        override fun build(): Component = component
        override fun build(index: Int): Component = component
    }

    private class StringComponentBuilder(
        string: String,
        val data: ComponentData,
        map: Map<String, List<Component>>
    ): ComponentBuilder {
        private val listComponent: List<Component>
        init {
            listComponent = parse(string, map).map {
                it.font(data.font).color(data.color).decorations(data.decoration)
            }
        }


        override fun length() = listComponent.size
        override fun build(index: Int) = if (listComponent.isNotEmpty()) listComponent[index] else Component.empty()
        override fun build(): Component {
            var empty = Component.empty()
            listComponent.forEach {
                empty = empty.append(it)
            }
            return empty
        }
    }

    private class GradientComponentBuilder(
        string: String,
        private val data: ComponentData,
        gradient: GradientData,
        map: Map<String, List<Component>>
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

            val comp = parse(string, map)
            comp.mapIndexed { index, component ->
                val t = index.toDouble() / comp.size.toDouble()
                component.color(TextColor.color(
                    fromRed + (calRed * t).toInt(),
                    fromGreen + (calGreen * t).toInt(),
                    fromBlue + (calBlue * t).toInt()
                )).font(data.font).decorations(data.decoration)
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
    fun createIterator(t: T, map: Map<String, List<Component>> = emptyMap()) = try {
        ComponentIteratorImpl(t, map)
    } catch (ex: Exception) {
        null
    }
    fun createComponent(t: T, map: Map<String, List<Component>> = emptyMap()): Component? = try {
        var result: Component = Component.empty()
        components.forEach {
            result = result.append(it(t,map).build())
        }
        result
    } catch (ex: Exception) {
        null
    }


    interface ComponentIterator {
        fun hasNext(): Boolean
        fun nextLine(): Component
    }

    inner class ComponentIteratorImpl(private val t: T, map: Map<String, List<Component>>): ComponentIterator {
        private var index1 = 0
        private var index2 = 0

        private val parsedComponent = components.map {
            it(t,map)
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
