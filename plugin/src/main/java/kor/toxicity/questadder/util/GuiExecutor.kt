package kor.toxicity.questadder.util

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface GuiExecutor {
    fun initialize(inventory: Inventory)
    fun onClick(inventory: Inventory, isPlayerInventory: Boolean, clickedItem: ItemStack, clickedSlot: Int, action: MouseButton)
    fun onEnd(inventory: Inventory)
}