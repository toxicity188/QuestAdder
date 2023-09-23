package kor.toxicity.questadder.nms.v1_19_R3

import com.mojang.datafixers.util.Pair
import com.mojang.math.Transformation
import eu.endercentral.crazy_advancements.JSONMessage
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay
import eu.endercentral.crazy_advancements.advancement.ToastNotification
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.nms.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.*
import net.minecraft.server.network.PlayerConnection
import net.minecraft.world.entity.*
import net.minecraft.world.entity.decoration.EntityArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R3.CraftServer
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

class NMSImpl: NMS {

    override fun removePlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.b.run {
            a(PacketPlayOutEntityDestroy(target.entityId))
            a(ClientboundPlayerInfoRemovePacket(listOf(target.uniqueId)))
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
        if (!map.register("questadder",obj)) QuestAdderBukkit.warn("unable to register command: $name")
        return object: RuntimeCommand {
            override fun unregister() {
                map.knownCommands.remove("questadder:$name")
                map.knownCommands.remove(name)
            }
        }
    }
    override fun updateCommand() {
        (Bukkit.getServer() as CraftServer).run {
            syncCommands()
            val dispatcher = server.au.b().d
            val minecraft = server.vanillaCommandDispatcher
            for (child in minecraft.a().root.children) {
                dispatcher.a().root.addChild(child)
            }
        }
    }
    override fun createArmorStand(player: Player, location: Location): VirtualArmorStand {
        return VirtualArmorStandImpl(player, location)
    }
    private abstract class VirtualEntityImpl<T: Entity>(player: Player, protected val entity: T): VirtualEntity {
        protected val connection: PlayerConnection = (player as CraftPlayer).handle.b.apply {
            a(PacketPlayOutSpawnEntity(entity))
        }
        override fun teleport(location: Location) {
            entity.a(location.x,location.y,location.z,location.yaw,location.pitch)
            connection.a(PacketPlayOutEntityTeleport(entity))
        }
        override fun remove() {
            connection.a(PacketPlayOutEntityDestroy(entity.af()))
        }
    }
    private abstract class VirtualDisplayImpl<T: Display>(player: Player, entity: T): VirtualEntityImpl<T>(player,entity), VirtualDisplay {
        override fun setSize(x: Double, y: Double, z: Double) {
            entity.a(Transformation(null,null, Vector3f(x.toFloat(),y.toFloat(),z.toFloat()),null))
            connection.a(PacketPlayOutEntityMetadata(entity.af(),entity.aj().c()))
        }
    }
    private class VirtualArmorStandImpl(player: Player, location: Location): VirtualEntityImpl<EntityArmorStand>(player,EntityArmorStand(EntityTypes.d,(location.world as CraftWorld).handle).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
        j(true)
        n(true)
    }), VirtualArmorStand {
        init {
            connection.a(PacketPlayOutEntityMetadata(entity.af(),entity.aj().c()))
        }
        override fun setText(text: Component) {
            (entity.bukkitEntity as ArmorStand).customName(text)
            connection.a(PacketPlayOutEntityMetadata(entity.af(),entity.aj().c()))
        }
        override fun setItem(itemStack: ItemStack) {
            val item = CraftItemStack.asNMSCopy(itemStack)
            entity.setItemSlot(EnumItemSlot.f, item,true)
            connection.a(PacketPlayOutEntityEquipment(entity.af(), listOf(Pair(EnumItemSlot.f,item))))
        }
    }
    override fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay {
        return VirtualItemDisplayImpl(player, location)
    }

    private class VirtualItemDisplayImpl(player: Player, location: Location): VirtualDisplayImpl<Display.ItemDisplay>(player,Display.ItemDisplay(EntityTypes.ae,(location.world as CraftWorld).handle).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
    }), VirtualItemDisplay {
        override fun setItem(itemStack: ItemStack) {
            entity.a(CraftItemStack.asNMSCopy(itemStack))
            connection.a(PacketPlayOutEntityMetadata(entity.af(),entity.aj().c()))
        }
    }

    override fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay {
        return VirtualTextDisplayImpl(player, location)
    }
    private class VirtualTextDisplayImpl(player: Player ,location: Location): VirtualDisplayImpl<Display.TextDisplay>(player,Display.TextDisplay(
        EntityTypes.aX,(location.world as CraftWorld).handle).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
        a(Display.BillboardConstraints.d)
    }), VirtualTextDisplay {
        override fun setText(text: Component) {
            (entity.bukkitEntity as TextDisplay).text(text)
            connection.a(PacketPlayOutEntityMetadata(entity.af(),entity.aj().c()))
        }
    }
    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_19_R3
    }
    override fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>) {
        val packet = PacketPlayOutEntityEquipment(player.entityId, listOf(Pair(EnumItemSlot.a,CraftItemStack.asNMSCopy(itemStack))))
        targetPlayer.forEach {
            (it as CraftPlayer).handle.b.a(packet)
        }
    }
    override fun changePosition(player: Player, location: Location) {
        (player as CraftPlayer).handle.b.a(PacketPlayOutPosition(
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            RelativeMovement.entries.toSet(),
            player.entityId
        ))
    }
}