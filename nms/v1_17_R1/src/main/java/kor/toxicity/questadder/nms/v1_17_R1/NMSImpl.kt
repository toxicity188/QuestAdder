package kor.toxicity.questadder.nms.v1_17_R1

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Pair
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.nms.*
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.DataWatcherObject
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.server.level.EntityPlayer
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.EnumItemSlot
import net.minecraft.world.entity.decoration.EntityArmorStand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NMSImpl: NMS {

    override fun removePlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.b.run {
            sendPacket(PacketPlayOutEntityDestroy(target.entityId))
            sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, (target as CraftPlayer).handle))
        }
    }
    override fun sendAdvancementMessage(player: Player, itemStack: ItemStack, component: Component) {
        //TODO find a new toast API to show it.
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
                val knownCommands = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
                knownCommands.isAccessible = true
                val m = knownCommands.get(map) as MutableMap<*, *>
                m.remove("questadder:$name")
                m.remove(name)
                knownCommands.isAccessible = false
            }
        }
    }
    override fun updateCommand() {
        (Bukkit.getServer() as CraftServer).run {
            syncCommands()
            val dispatcher = server.commandDispatcher
            val minecraft = server.vanillaCommandDispatcher
            for (child in minecraft.a().root.children) {
                dispatcher.a().root.addChild(child)
            }
        }
    }
    override fun createArmorStand(player: Player, location: Location): VirtualArmorStand {
        return VirtualArmorStandImpl(player, location)
    }
    private class VirtualArmorStandImpl(player: Player, location: Location): VirtualArmorStand {
        private val display = EntityArmorStand(EntityTypes.c,(location.world as CraftWorld).handle).apply {
            setLocation(location.x,location.y,location.z,location.yaw,location.pitch)
            isInvisible = true
            customNameVisible = true
        }
        private val connection = (player as CraftPlayer).handle.b.apply {
            sendPacket(PacketPlayOutSpawnEntity(display))
            sendPacket(PacketPlayOutEntityMetadata(display.id,display.dataWatcher,true))
        }

        override fun teleport(location: Location) {
            display.setLocation(location.x,location.y,location.z,location.yaw,location.pitch)
            connection.sendPacket(PacketPlayOutEntityTeleport(display))
        }
        override fun remove() {
            connection.sendPacket(PacketPlayOutEntityDestroy(display.id))
        }

        override fun setText(text: Component) {
            (display.bukkitEntity as ArmorStand).customName(text)
            connection.sendPacket(PacketPlayOutEntityMetadata(display.id, display.dataWatcher,true))
        }

        override fun setItem(itemStack: ItemStack) {
            val item = CraftItemStack.asNMSCopy(itemStack)
            display.setSlot(EnumItemSlot.f, item,true)
            connection.sendPacket(PacketPlayOutEntityEquipment(display.id, listOf(Pair(EnumItemSlot.f,item))))
        }
    }
    override fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay {
        throw UnsupportedOperationException("unsupported minecraft version.")
    }

    override fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay {
        throw UnsupportedOperationException("unsupported minecraft version.")
    }

    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_17_R1
    }
    override fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>) {
        val packet = PacketPlayOutEntityEquipment(player.entityId, listOf(Pair(EnumItemSlot.a,CraftItemStack.asNMSCopy(itemStack))))
        targetPlayer.forEach {
            (it as CraftPlayer).handle.b.sendPacket(packet)
        }
    }
    override fun changePosition(player: Player, location: Location) {
        (player as CraftPlayer).handle.b.sendPacket(PacketPlayOutPosition(
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            PacketPlayOutPosition.EnumPlayerTeleportFlags.entries.toSet(),
            player.entityId,
            true
        ))
    }

    override fun createFakePlayer(player: Player, location: Location, skin: GameProfile): VirtualPlayer {
        return VirtualPlayerImpl(player, location, skin)
    }
    private class VirtualPlayerImpl(player: Player, location: Location, skin: GameProfile): VirtualPlayer {
        private val entity = EntityPlayer((Bukkit.getServer() as CraftServer).server,(location.world as CraftWorld).handle,skin).apply {
            setLocation(location.x,location.y,location.z,location.yaw,location.pitch)
        }
        private val connection = (player as CraftPlayer).handle.b.apply {
            sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entity))
            sendPacket(PacketPlayOutNamedEntitySpawn(entity))
            val watcher = entity.dataWatcher
            watcher.set(DataWatcherObject(17, DataWatcherRegistry.a), 127)
            sendPacket(PacketPlayOutEntityMetadata(entity.id, watcher, true))
        }

        override fun teleport(location: Location) {
            entity.setLocation(location.x,location.y,location.z,location.yaw,location.pitch)
        }
        override fun remove() {
            connection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e,entity))
            connection.sendPacket(PacketPlayOutEntityDestroy(entity.id))
        }
    }

    override fun getGameProfile(player: Player): GameProfile {
        return (player as CraftPlayer).handle.profile
    }
}