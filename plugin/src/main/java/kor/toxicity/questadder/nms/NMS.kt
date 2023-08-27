package kor.toxicity.questadder.nms

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface NMS {
    fun removePlayer(player: Player, target: Player)
    fun createArmorStand(player: Player, location: Location): VirtualArmorStand
    fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay
    fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay
    fun createCommand(name: String, executor: CommandExecutor): RuntimeCommand
    fun sendAdvancementMessage(player: Player, itemStack: ItemStack, component: Component)
    fun getVersion(): NMSVersion
    fun updateCommand()
    fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>)
}