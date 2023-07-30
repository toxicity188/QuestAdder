package kor.toxicity.questadder.util

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class GuiWrapper(val title: Component, size: Int) {
    val size = size.coerceAtLeast(1).coerceAtMost(6) * 9

    val map: MutableMap<Int,ItemStack> = HashMap()

    fun open(player: Player, executor: GuiExecutor? = null) = GuiHolder(player,executor)

    inner class GuiHolder(player: Player, var executor: GuiExecutor? = null): InventoryHolder {
        private val inv = Bukkit.createInventory(this,size,title).apply {
            map.forEach {
                setItem(it.key,it.value)
            }
        }
        init {
            player.openInventory(inv)
            executor?.initialize(inv)
        }
        override fun getInventory(): Inventory {
            return inv
        }
    }
}