package kor.toxicity.questadder.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

val GREEN: TextColor = NamedTextColor.GREEN
val GRAY: TextColor = NamedTextColor.GRAY
val WHITE: TextColor = NamedTextColor.WHITE
val YELLOW: TextColor = NamedTextColor.YELLOW

fun String.asComponent() = Component.text(this)
fun String.asComponent(color: TextColor) = asComponent().color(color)
fun String.asClearComponent() = asComponent().clear()

fun Component.deepDecorate(textDecoration: TextDecoration): Component {
    val children = children().toMutableList()
    for ((index,child) in children.withIndex()) {
        children[index] = child.deepDecorate(textDecoration)
    }
    return children(children).decorate(textDecoration)
}
fun Component.deepDecorations(textDecoration: Map<TextDecoration,TextDecoration.State>): Component {
    val children = children().toMutableList()
    for ((index,child) in children.withIndex()) {
        children[index] = child.deepDecorations(textDecoration)
    }
    return children(children).decorations(textDecoration)
}
fun Component.deepColor(color: TextColor?): Component {
    val children = children().toMutableList()
    for ((index,child) in children.withIndex()) {
        children[index] = child.deepColor(color)
    }
    return children(children).color(color)
}

fun Component.onlyText(): String {
    val builder = StringBuilder()
    fun append(component: Component) {
        for (child in component.children()) {
            append(child)
        }
        if (component is TextComponent) builder.append(component.content())
    }
    append(this)
    return builder.toString()
}