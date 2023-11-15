package kor.toxicity.questadder.platform

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.text.DecimalFormat

class PaperPlatformAdapter: PlatformAdapter {
    override fun setDisplay(itemMeta: ItemMeta, component: Component?) {
        itemMeta.displayName(component)
    }

    override fun setLore(itemMeta: ItemMeta, component: List<Component>?) {
        itemMeta.lore(component)
    }

    override fun createInventory(holder: InventoryHolder, name: Component, size: Int): Inventory {
        return Bukkit.createInventory(holder, size, name)
    }

    override fun changeSign(player: Player, location: Location, array: List<Component>) {
        player.sendSignChange(location, array)
    }

    override fun getItemName(itemStack: ItemStack): Component {
        return (itemStack.itemMeta?.displayName() ?: Component.text(itemStack.type.toString().lowercase())).append(
            Component.space()).append(Component.text("x${DecimalFormat.getInstance().format(itemStack.amount)}").color(NamedTextColor.GREEN).decorate(
            TextDecoration.BOLD,
            TextDecoration.ITALIC)).hoverEvent(itemStack)
    }

    override fun getLore(itemMeta: ItemMeta): List<Component> {
        return itemMeta.lore() ?: ArrayList()
    }

    override fun kick(player: Player, message: Component) {
        player.kick(message)
    }

    override fun getTargetPlatformName(): String {
        return "Paper"
    }
}
