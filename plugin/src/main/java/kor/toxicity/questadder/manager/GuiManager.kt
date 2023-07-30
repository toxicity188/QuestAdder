package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.util.GuiWrapper
import kor.toxicity.questadder.util.MouseButton
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

object GuiManager: QuestAdderManager {

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun click(e: InventoryClickEvent) {
                val clicked = e.view.topInventory
                val holder = clicked.holder as? GuiWrapper.GuiHolder ?: return
                e.isCancelled = true
                holder.executor?.onClick(
                    holder.inventory,
                    clicked == e.whoClicked.inventory,
                    e.currentItem ?: return,
                    e.slot,
                    if (e.isLeftClick) {
                        if (e.isShiftClick) MouseButton.SHIFT_LEFT else MouseButton.LEFT
                    } else if (e.isRightClick) {
                        if (e.isShiftClick) MouseButton.SHIFT_RIGHT else MouseButton.RIGHT
                    } else {
                        MouseButton.OTHER
                    }
                )
            }
            @EventHandler
            fun end(e: InventoryCloseEvent) {
                val inv = e.inventory
                val holder = inv.holder as? GuiWrapper.GuiHolder ?: return
                holder.executor?.onEnd(inv)
            }
        },adder)
    }

    override fun reload(adder: QuestAdder) {
        closeAll()
    }

    override fun end(adder: QuestAdder) {
        closeAll()
    }
    private fun closeAll() {
        Bukkit.getOnlinePlayers().forEach {
            if (it.openInventory.topInventory.holder is GuiWrapper.GuiHolder) it.closeInventory()
        }
    }
}