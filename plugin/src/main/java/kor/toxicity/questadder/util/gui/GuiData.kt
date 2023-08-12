package kor.toxicity.questadder.util.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class GuiData(val gui: Gui, val inventory: Inventory, private val executor: GuiExecutor, val player: Player) {
    fun reopen() {
        gui.open(player,executor)
    }
}