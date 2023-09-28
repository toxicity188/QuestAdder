package kor.toxicity.questadder.nms.v1_19_R2

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Pair
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
import net.minecraft.network.protocol.game.PacketPlayOutPosition.EnumPlayerTeleportFlags
import net.minecraft.network.syncher.DataWatcherObject
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.server.level.EntityPlayer
import net.minecraft.server.network.PlayerConnection
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.EnumItemSlot
import net.minecraft.world.entity.decoration.EntityArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R2.CraftServer
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

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
            val dispatcher = server.aB()
            val minecraft = server.vanillaCommandDispatcher
            for (child in minecraft.a().root.children) {
                dispatcher.a().root.addChild(child)
            }
        }
    }
    override fun createArmorStand(player: Player, location: Location): VirtualArmorStand {
        return VirtualArmorStandImpl(player, location)
    }
    private class VirtualArmorStandImpl(player: Player, location: Location): VirtualEntityImpl<EntityArmorStand>(player, EntityArmorStand(EntityTypes.d,(location.world as CraftWorld).handle).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
        j(true)
        n(true)
    }), VirtualArmorStand {
        init {
            connection.a(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
        }
        override fun setText(text: Component) {
            (entity.bukkitEntity as ArmorStand).customName(text)
            connection.a(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
        }
        override fun setItem(itemStack: ItemStack) {
            val item = CraftItemStack.asNMSCopy(itemStack)
            entity.setItemSlot(EnumItemSlot.f, item,true)
            connection.a(PacketPlayOutEntityEquipment(entity.ah(), listOf(Pair(EnumItemSlot.f,item))))
        }
    }
    override fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay {
        throw UnsupportedOperationException("unsupported minecraft version.")
    }
    override fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay {
        throw UnsupportedOperationException("unsupported minecraft version.")
    }
    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_19_R2
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
            EnumPlayerTeleportFlags.entries.toSet(),
            player.entityId,
            true
        ))
    }

    override fun createFakePlayer(player: Player, location: Location, skin: GameProfile): VirtualPlayer {
        return VirtualPlayerImpl(player, location, skin)
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
            connection.a(PacketPlayOutEntityDestroy(entity.ah()))
        }
    }
    private class VirtualPlayerImpl(player: Player, location: Location, skin: GameProfile): VirtualEntityImpl<EntityPlayer>(player,EntityPlayer((Bukkit.getServer() as CraftServer).server,(location.world as CraftWorld).handle,skin).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
    }), VirtualPlayer {
        init {
            connection.a(ClientboundPlayerInfoUpdatePacket(EnumSet.noneOf(ClientboundPlayerInfoUpdatePacket.a::class.java).apply {
                add(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.a.entries[0])
            }, listOf(entity)))
            connection.a(PacketPlayOutNamedEntitySpawn(entity))
            val watcher = entity.al()
            watcher.b(DataWatcherObject(17, DataWatcherRegistry.a), 127)
            connection.a(PacketPlayOutEntityMetadata(entity.ah(),watcher.c()))
        }
        override fun remove() {
            connection.a(ClientboundPlayerInfoRemovePacket(listOf(entity.cs())))
            super.remove()
        }
    }
    override fun getGameProfile(player: Player): GameProfile {
        return (player as CraftPlayer).handle.fD()
    }
}