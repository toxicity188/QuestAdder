package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdderBukkit
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer


val SPACE_FONT = Key.key("questadder:space")
val BUILD_FONT = Key.key("questadder:build")
val LEGACY_SPACE_FONT = Key.key("questadder:legacy_space")
val GUI_FONT = Key.key("questadder:gui")
val FADE_COMPONENT_UPPER = Component.text("☆").font(Key.key("questadder:fade"))
val FADE_COMPONENT_UNDER = Component.text("★").font(Key.key("questadder:fade"))

val RED: TextColor = NamedTextColor.RED
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
    return PlainTextComponentSerializer.plainText().serialize(this)
}

fun Int.parseToSpaceComponent() = if (QuestAdderBukkit.nms.getVersion().version >= 19) (0xD0000 + this).parseChar().asComponent().font(SPACE_FONT) else (0xFFC00 + this).parseChar().asComponent().font(LEGACY_SPACE_FONT)
