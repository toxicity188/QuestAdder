package kor.toxicity.questadder.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

val GOLD: TextColor = NamedTextColor.GOLD
val RED: TextColor = NamedTextColor.RED
val WHITE: TextColor = NamedTextColor.WHITE

fun String.asComponent() = Component.text(this)
fun String.asComponent(color: TextColor) = asComponent().color(color)