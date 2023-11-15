package kor.toxicity.questadder.platform

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

interface PlatformAdapter {
    fun setDisplay(itemMeta: ItemMeta, component: Component?)
    fun setLore(itemMeta: ItemMeta, component: List<Component>?)
    fun createInventory(holder: InventoryHolder, name: Component, size: Int): Inventory
    fun changeSign(player: Player, location: Location, array: List<Component>)
    fun getItemName(itemStack: ItemStack): Component
    fun getLore(itemMeta: ItemMeta): List<Component>
    fun kick(player: Player, message: Component)

    fun getTargetPlatformName(): String
}
