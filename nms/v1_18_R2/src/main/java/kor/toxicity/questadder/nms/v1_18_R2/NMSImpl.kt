package kor.toxicity.questadder.nms.v1_18_R2

import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay
import eu.endercentral.crazy_advancements.advancement.ToastNotification
import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.nms.NMS
import kor.toxicity.questadder.nms.RuntimeCommand
import kor.toxicity.questadder.nms.VirtualArmorStand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minecraft.network.protocol.game.*
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.decoration.EntityArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_18_R2.CraftServer
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NMSImpl: NMS {

    override fun removePlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.b.run {
            a(PacketPlayOutEntityDestroy(target.entityId))
            a(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, (target as CraftPlayer).handle))
        }
    }

    override fun sendAdvancementMessage(player: Player, itemStack: ItemStack, component: Component) {
        ToastNotification(
            itemStack,
            LegacyComponentSerializer.legacySection().serialize(component),
            AdvancementDisplay.AdvancementFrame.GOAL
        ).send(player)
    }

    override fun createCommand(name: String, executor: CommandExecutor): RuntimeCommand {
        val map = (Bukkit.getServer() as CraftServer).commandMap
        val obj = object : Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                return executor.onCommand(sender,this,commandLabel,args)
            }
        }
        if (!map.register("questadder",obj)) QuestAdder.warn("unable to register command: $name")
        return object: RuntimeCommand {
            override fun unregister() {
                map.knownCommands.remove("questadder:$name")
                map.knownCommands.remove(name)
            }
        }
    }

    override fun createArmorStand(player: Player, location: Location): VirtualArmorStand {
        return VirtualArmorStandImpl(player, location)
    }
    private class VirtualArmorStandImpl(player: Player, location: Location): VirtualArmorStand {
        private val display = EntityArmorStand(EntityTypes.c,(location.world as CraftWorld).handle).apply {
            a(location.x,location.y,location.z,location.yaw,location.pitch)
            j(true)
            n(true)
        }
        private val connection = (player as CraftPlayer).handle.b.apply {
            a(PacketPlayOutSpawnEntity(display))
        }

        override fun teleport(location: Location) {
            display.a(location.x,location.y,location.z,location.yaw,location.pitch)
            connection.a(PacketPlayOutEntityTeleport(display))
        }
        override fun remove() {
            connection.a(PacketPlayOutEntityDestroy(display.ae()))
        }

        override fun setText(text: Component) {
            (display.bukkitEntity as ArmorStand).customName(text)
            connection.a(PacketPlayOutEntityMetadata(display.ae(),display.ai(),true))
        }
    }
}