package kor.toxicity.questadder.nms

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player

interface NMS {
    fun removePlayer(player: Player, target: Player)
    fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay
    fun createCommand(name: String, executor: CommandExecutor): RuntimeCommand
    fun sendAdvancementMessage(player: Player, component: Component)
}