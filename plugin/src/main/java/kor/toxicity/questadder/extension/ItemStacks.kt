package kor.toxicity.questadder.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.inventory.ItemStack
import java.util.*


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
    Component.space()).append("x$amount".asComponent(NamedTextColor.GREEN).decorate(TextDecoration.BOLD,TextDecoration.ITALIC))

fun ItemStack.getNameString() = "${itemMeta.displayName()?.let { c ->
    LegacyComponentSerializer.legacySection().serialize(c)
} ?: type.toString().lowercase()} x$amount"