package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.gui.GuiHolder
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.util.gui.Gui
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

object GuiManager: QuestAdderManager {

    override fun start(adder: QuestAdderBukkit) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun click(e: InventoryClickEvent) {
                val clicked = e.view.topInventory
                val holder = clicked.holder as? GuiHolder ?: return
                e.isCancelled = true
                holder.executor.click(
                    holder.data,
                    e.currentItem ?: return,
                    e.slot,
                    e.inventory == e.whoClicked.inventory,
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
                val holder = inv.holder as? GuiHolder ?: return
                holder.executor.end(holder.data)
            }
        },adder)
    }

    override fun reload(adder: QuestAdderBukkit) {
        QuestAdderBukkit.task {
            closeAll()
        }
    }

    override fun end(adder: QuestAdderBukkit) {
        closeAll()
    }
    private fun closeAll() {
        Bukkit.getOnlinePlayers().forEach {
            if (it.openInventory.topInventory.holder is GuiHolder) it.closeInventory()
        }
    }
}