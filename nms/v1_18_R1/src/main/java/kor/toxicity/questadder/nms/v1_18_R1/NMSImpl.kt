package kor.toxicity.questadder.nms.v1_18_R1

import com.mojang.datafixers.util.Pair
import eu.endercentral.crazy_advancements.JSONMessage
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay
import eu.endercentral.crazy_advancements.advancement.ToastNotification
import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.nms.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.*
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.EnumItemSlot
import net.minecraft.world.entity.decoration.EntityArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_18_R1.CraftServer
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage
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
            object : JSONMessage(null) {
                override fun getBaseComponent(): IChatBaseComponent {
                    return CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component))
                }

                override fun toString(): String {
                    return PlainTextComponentSerializer.plainText().serialize(component)
                }
            },
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
    override fun updateCommand() {
        (Bukkit.getServer() as CraftServer).syncCommands()
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
            a(PacketPlayOutEntityMetadata(display.ae(),display.ai(),true))
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

        override fun setItem(itemStack: ItemStack) {
            val item = CraftItemStack.asNMSCopy(itemStack)
            display.setItemSlot(EnumItemSlot.f, item,true)
            connection.a(PacketPlayOutEntityEquipment(display.ae(), listOf(Pair(EnumItemSlot.f,item))))
        }
    }
    override fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay {
        throw UnsupportedOperationException("unsupported minecraft version.")
    }

    override fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay {
        throw UnsupportedOperationException("unsupported minecraft version.")
    }

    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_18_R1
    }
    override fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>) {
        val packet = PacketPlayOutEntityEquipment(player.entityId, listOf(Pair(EnumItemSlot.a,CraftItemStack.asNMSCopy(itemStack))))
        targetPlayer.forEach {
            (it as CraftPlayer).handle.b.a(packet)
        }
    }
}