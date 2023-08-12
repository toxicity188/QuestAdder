package kor.toxicity.questadder.util.gui

import org.bukkit.inventory.ItemStack

interface GuiExecutor {
    fun initialize(data: GuiData)
    fun click(data: GuiData, clickedItem: ItemStack, clickedSlot: Int, isPlayerInventory: Boolean, button: MouseButton)
    fun end(data: GuiData)
}