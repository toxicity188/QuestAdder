package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdderBukkit
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

fun ItemStack.getNameComponent() = QuestAdderBukkit.platform.getItemName(this)

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

fun ItemStack.getNameString() = LegacyComponentSerializer.legacySection().serialize(getNameComponent())
