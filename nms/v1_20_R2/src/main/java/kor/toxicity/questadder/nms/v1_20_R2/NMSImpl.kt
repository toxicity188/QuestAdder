package kor.toxicity.questadder.nms.v1_20_R2

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
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
import net.minecraft.network.syncher.DataWatcherObject
import net.minecraft.network.syncher.DataWatcherRegistry
import net.minecraft.server.level.EntityPlayer
import net.minecraft.server.level.WorldServer
import net.minecraft.server.network.PlayerConnection
import net.minecraft.world.entity.*
import net.minecraft.world.entity.decoration.EntityArmorStand
import net.minecraft.world.entity.item.EntityItem
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.craftbukkit.v1_20_R2.CraftServer
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftItem
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f
import java.util.*

class NMSImpl: NMS {

    override fun removePlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.c.run {
            b(PacketPlayOutEntityDestroy(target.entityId))
            b(ClientboundPlayerInfoRemovePacket(listOf(target.uniqueId)))
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
                val field = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
                field.isAccessible = true
                val getMap = field.get(map) as MutableMap<*,*>
                getMap.remove("questadder:$name")
                getMap.remove(name)
                field.isAccessible = false
            }
        }
    }
    override fun updateCommand() {
        (Bukkit.getServer() as CraftServer).run {
            syncCommands()
            val dispatcher = server.aC()
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
        protected val connection: PlayerConnection = (player as CraftPlayer).handle.c.apply {
            b(PacketPlayOutSpawnEntity(entity))
        }
        override fun teleport(location: Location) {
            entity.a(location.x,location.y,location.z,location.yaw,location.pitch)
            connection.b(PacketPlayOutEntityTeleport(entity))
        }
        override fun remove() {
            connection.b(PacketPlayOutEntityDestroy(entity.ah()))
        }
    }
    private class VirtualArmorStandImpl(player: Player, location: Location): VirtualEntityImpl<EntityArmorStand>(player,EntityArmorStand(EntityTypes.d,(location.world as CraftWorld).handle).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
        j(true)
        n(true)
    }), VirtualArmorStand {
        init {
            connection.b(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
        }
        override fun setText(text: Component) {
            entity.b(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(text)))
            connection.b(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
        }
        override fun setItem(itemStack: ItemStack) {
            val item = CraftItemStack.asNMSCopy(itemStack)
            entity.setItemSlot(EnumItemSlot.f, item,true)
            connection.b(PacketPlayOutEntityEquipment(entity.ah(), listOf(Pair(EnumItemSlot.f,item))))
        }
    }
    override fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay {
        return VirtualItemDisplayImpl(player, location)
    }

    private abstract class VirtualDisplayImpl<T: Display>(player: Player, entity: T): VirtualEntityImpl<T>(player,entity), VirtualDisplay {
        override fun setSize(x: Double, y: Double, z: Double) {
            entity.a(Transformation(null,null, Vector3f(x.toFloat(),y.toFloat(),z.toFloat()),null))
            connection.b(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
        }
    }

    private class VirtualItemDisplayImpl(player: Player, location: Location): VirtualDisplayImpl<Display.ItemDisplay>(player,Display.ItemDisplay(EntityTypes.ae,(location.world as CraftWorld).handle).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
    }), VirtualItemDisplay {
        override fun setItem(itemStack: ItemStack) {
            entity.a(CraftItemStack.asNMSCopy(itemStack))
            connection.b(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
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
            entity.c(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(text)))
            connection.b(PacketPlayOutEntityMetadata(entity.ah(),entity.al().c()))
        }
    }
    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_20_R2
    }

    override fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>) {
        val packet = PacketPlayOutEntityEquipment(player.entityId, listOf(Pair(EnumItemSlot.a,CraftItemStack.asNMSCopy(itemStack))))
        targetPlayer.forEach {
            (it as CraftPlayer).handle.c.b(packet)
        }
    }

    override fun changePosition(player: Player, location: Location) {
        (player as CraftPlayer).handle.c.b(PacketPlayOutPosition(
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            RelativeMovement.entries.toSet(),
            player.entityId
        ))
    }

    override fun createFakePlayer(player: Player, location: Location, skin: GameProfile): VirtualPlayer {
        return VirtualPlayerImpl(player, location, skin)
    }
    private class VirtualPlayerImpl(player: Player, location: Location, skin: GameProfile): VirtualEntityImpl<EntityPlayer>(player, EntityPlayer((Bukkit.getServer() as CraftServer).server,(location.world as CraftWorld).handle,skin,null).apply {
        a(location.x,location.y,location.z,location.yaw,location.pitch)
    }), VirtualPlayer {
        init {
            connection.b(ClientboundPlayerInfoUpdatePacket(EnumSet.noneOf(ClientboundPlayerInfoUpdatePacket.a::class.java).apply {
                add(ClientboundPlayerInfoUpdatePacket.a.entries[0])
            },listOf(entity)))
            val watcher = entity.al()
            watcher.a(DataWatcherObject(17, DataWatcherRegistry.a), 127, true)
            connection.b(PacketPlayOutEntityMetadata(entity.ah(),watcher.c()))
        }

        override fun remove() {
            connection.b(ClientboundPlayerInfoRemovePacket(listOf(entity.cv())))
            super.remove()
        }
    }
    override fun getGameProfile(player: Player): GameProfile {
        return (player as CraftPlayer).handle.fQ()
    }

    override fun getProperties(gameProfile: GameProfile): PropertyMap {
        return gameProfile.properties
    }

    override fun createFakeItem(itemStack: ItemStack, location: Location): FakeItem {
        return FakeItemImpl(itemStack, location)
    }
    private class FakeItemImpl(itemStack: ItemStack, location: Location): FakeItem {
        val world: WorldServer = (location.world as CraftWorld).handle
        val handle = CraftItem(Bukkit.getServer() as CraftServer, EntityItem(EntityTypes.ad, world).apply {
            h = 10
            a(location.x, location.y, location.z, location.yaw, location.pitch)
            a(CraftItemStack.asNMSCopy(itemStack))
        })

        override fun getItem(): Item {
            return handle
        }

        override fun spawn() {
            world.addFreshEntity(handle.handle, CreatureSpawnEvent.SpawnReason.CUSTOM)
        }
    }
}
