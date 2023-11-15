package kor.toxicity.questadder.platform

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.text.DecimalFormat

class SpigotPlatformAdapter: PlatformAdapter {
    private val legacy = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build()
    override fun setDisplay(itemMeta: ItemMeta, component: Component?) {
        itemMeta.setDisplayName(component?.let {
            legacy.serialize(it)
        })
    }

    override fun setLore(itemMeta: ItemMeta, component: List<Component>?) {
        itemMeta.lore = component?.map {
            legacy.serialize(it)
        }
    }

    override fun createInventory(holder: InventoryHolder, name: Component, size: Int): Inventory {
        return Bukkit.createInventory(holder, size, legacy.serialize(name))
    }

    override fun changeSign(player: Player, location: Location, array: List<Component>) {
        player.sendSignChange(location, array.map {
            legacy.serialize(it)
        }.toTypedArray())
    }

    override fun getItemName(itemStack: ItemStack): Component {
        return Component.text("${itemStack.itemMeta?.displayName ?: itemStack.type.toString().lowercase()} x${DecimalFormat.getInstance().format(itemStack.amount)}")
    }

    override fun getLore(itemMeta: ItemMeta): List<Component> {
        return (itemMeta.lore ?: ArrayList()).map {
            legacy.deserialize(it)
        }
    }

    override fun kick(player: Player, message: Component) {
        player.kickPlayer(legacy.serialize(message))
    }

    override fun getTargetPlatformName(): String {
        return "Spigot"
    }
}
