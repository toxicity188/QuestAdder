package kor.toxicity.questadder.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.util.*


val QUEST_ADDER_ITEM_KEY = NamespacedKey.fromString("questadder.item.key")!!
val QUEST_ADDER_SENDER_KEY = NamespacedKey.fromString("questadder.sender.key")!!

fun ItemStack.serializeToString(): String {
    return Base64.getEncoder().encodeToString(serializeAsBytes())
}
fun String.deserializeToItemStack(): ItemStack? {
    return try {
        ItemStack.deserializeBytes((Base64.getDecoder().decode(this)))
    } catch (ex: Exception) {
        null
    }
}

fun ItemStack.getNameComponent() = (itemMeta.displayName() ?: type.toString().lowercase().asClearComponent()).append(
    Component.space()).append("x$amount".asComponent(NamedTextColor.GREEN).decorate(TextDecoration.BOLD,TextDecoration.ITALIC)).hoverEvent(this)

fun List<ItemStack>.getNameComponent(): Component {
    var component = Component.empty()
    val comma = Component.text(", ").color(WHITE)
    for ((index,item) in withIndex()) {
        component = component.append(item.getNameComponent())
        if (index < lastIndex) component = component.append(comma)
    }
    return component
}
fun List<ItemStack>.getNameString(): String {
    val component = StringBuilder()
    val comma = ", "
    for ((index,item) in withIndex()) {
        component.append(item.getNameString())
        if (index < lastIndex) component.append(comma)
    }
    return component.toString()
}

fun ItemStack.getNameString() = "${itemMeta.displayName()?.let { c ->
    LegacyComponentSerializer.legacySection().serialize(c)
} ?: type.toString().lowercase()} x$amount"
