package kor.toxicity.questadder.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import java.util.regex.Pattern

val GOLD: TextColor = NamedTextColor.GOLD
val RED: TextColor = NamedTextColor.RED
val WHITE: TextColor = NamedTextColor.WHITE
val YELLOW: TextColor = NamedTextColor.YELLOW

fun String.asComponent() = Component.text(this)
fun String.asComponent(color: TextColor) = asComponent().color(color)

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
fun Component.deepColor(color: TextColor): Component {
    val children = children().toMutableList()
    for ((index,child) in children.withIndex()) {
        children[index] = child.deepColor(color)
    }
    return children(children).color(color)
}