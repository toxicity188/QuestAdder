package kor.toxicity.questadder.nms

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
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
    fun createFakePlayer(player: Player, location: Location, skin: GameProfile): VirtualPlayer
    fun createCommand(name: String, executor: CommandExecutor): RuntimeCommand
    fun sendAdvancementMessage(player: Player, itemStack: ItemStack, component: Component)
    fun getVersion(): NMSVersion
    fun updateCommand()
    fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>)
    fun changePosition(player: Player, location: Location)
    fun getGameProfile(player: Player): GameProfile
    fun getProperties(gameProfile: GameProfile): PropertyMap

}