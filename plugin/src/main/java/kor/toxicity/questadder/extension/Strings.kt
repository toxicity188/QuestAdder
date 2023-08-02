package kor.toxicity.questadder.extension

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.regex.Pattern

private val EMPTY_DECORATION = mapOf(
    TextDecoration.ITALIC to TextDecoration.State.FALSE,
    TextDecoration.BOLD to TextDecoration.State.FALSE,
    TextDecoration.UNDERLINED to TextDecoration.State.FALSE,
    TextDecoration.STRIKETHROUGH to TextDecoration.State.FALSE,
    TextDecoration.OBFUSCATED to TextDecoration.State.FALSE
)

val ANNOTATION_PATTERN: Pattern = Pattern.compile("@((?<name>(\\w|_|-)+):(?<value>(\\w|[가-힣]|_|,|-)+))")
fun String.colored(): TextComponent {
    val str = replace('&','§').split('^')
    if (str.size % 2 == 0) return Component.text(this)
    var component = Component.text("", Style.style()
        .color(WHITE)
        .decorations(EMPTY_DECORATION)
        .build()
    )
    var styleBuilder: Component? = null
    for ((i,s) in str.withIndex()) {
        if (i % 2 == 1) {
            val input = s.split('|')
            var builder = Component.empty()
                .color(WHITE)
                .decorations(EMPTY_DECORATION)
            for (t in input) {
                val matcher = TEXT_STYLE_PATTERN.matcher(t)
                if (matcher.find()) TEXT_STYLE_MAP[matcher.group("type")]?.run {
                    builder = invoke(builder,matcher.group("style"))
                }
            }
            styleBuilder = builder
        } else {
            component = component.append(styleBuilder?.run {
                styleBuilder = null
                append(Component.text(s))
            } ?: Component.text(s))
        }
    }
    return component
}
fun List<String>.colored() = map {
    it.colored()
}
private val TEXT_STYLE_PATTERN = Pattern.compile("(?<type>(\\w)+):(?<style>(\\w|\\W)+)")
private val TEXT_STYLE_MAP: Map<String,(Component, String) -> Component> = mapOf(
    "color" to { component, s ->
        component.color(TextColor.fromHexString("#$s"))
    },
    "font" to { component, s ->
        component.font(Key.key(s))
    },
    "decorate" to { component, s ->
        var b = component
        for (textDecoration in s.split('/').mapNotNull {
            try {
                TextDecoration.valueOf(it.uppercase())
            } catch (ex: Exception) {
                null
            }
        }) {
            b = b.decorate(textDecoration)
        }
        b
    }
)

fun Component.clear() = decorations(EMPTY_DECORATION)
fun Component.deepClear() = clear().color(WHITE)