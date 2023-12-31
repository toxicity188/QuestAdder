package kor.toxicity.questadder.nms.v1_19_R2

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import com.mojang.datafixers.util.Pair
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.nms.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.advancements.*
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.item.ItemEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.craftbukkit.v1_19_R2.CraftServer
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftItem
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R2.help.SimpleHelpMap
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@Suppress("unused")
class NMSImpl: NMS {

    private val criterionMap = mapOf(
        "all" to Criterion()
    )
    private val advancementRequirement = arrayOf(arrayOf("all"))
    private val resourceLocation = ResourceLocation("questadder", "notification")
    private val progressMap = mapOf(resourceLocation to AdvancementProgress().apply {
        update(criterionMap, advancementRequirement)
        getCriterion("all")?.grant()
    })
    private val nmsChannel = NMSChannelImpl()

    override fun removePlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.connection.run {
            send(ClientboundPlayerInfoRemovePacket(listOf(target.uniqueId)))
            send(ClientboundRemoveEntitiesPacket(target.entityId))
        }
    }

    override fun spawnPlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.connection.run {
            val handle = (target as CraftPlayer).handle
            send(ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), listOf(handle)))
            send(ClientboundAddPlayerPacket(handle))
            handle.entityData.nonDefaultValues?.let {
                send(ClientboundSetEntityDataPacket(handle.id, it))
            }
        }
    }

    override fun createArmorStand(player: Player, location: Location): VirtualArmorStand {
        return VirtualArmorStandImpl(player, location)
    }

    override fun createItemDisplay(player: Player, location: Location): VirtualItemDisplay {
        throw UnsupportedOperationException("Unsupported version.")
    }

    override fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay {
        throw UnsupportedOperationException("Unsupported version.")
    }

    override fun createFakePlayer(player: Player, location: Location, skin: GameProfile): VirtualPlayer {
        return VirtualPlayerImpl(player, location, skin)
    }

    override fun createCommand(name: String, executor: CommandExecutor): RuntimeCommand {
        val map = (Bukkit.getServer() as CraftServer).commandMap
        val obj = object : Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>?): Boolean {
                return executor.onCommand(sender, this, commandLabel, args)
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

    override fun sendAdvancementMessage(player: Player, itemStack: ItemStack, type: ToastType, component: Component) {
        (player as CraftPlayer).handle.connection.send(ClientboundUpdateAdvancementsPacket(
            false,
            listOf(Advancement(
                resourceLocation,
                null,
                DisplayInfo(
                    CraftItemStack.asNMSCopy(itemStack),
                    CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)),
                    net.minecraft.network.chat.Component.empty(),
                    null,
                    when (type) {
                        ToastType.GOAL -> FrameType.GOAL
                        ToastType.TASK -> FrameType.TASK
                        ToastType.CHALLENGE -> FrameType.CHALLENGE
                    },
                    true,
                    false,
                    true
                ),
                AdvancementRewards.EMPTY,
                criterionMap,
                advancementRequirement
            )),
            emptySet(),
            progressMap
        ))
    }

    override fun getVersion(): NMSVersion {
        return NMSVersion.V1_19_R2
    }

    override fun updateCommand() {
        (Bukkit.getServer() as CraftServer).run {
            commandMap.registerServerAliases()
            (helpMap as SimpleHelpMap).initializeCommands()
            syncCommands()
        }
    }

    override fun changeFakeItemInHand(player: Player, itemStack: ItemStack, targetPlayer: Collection<Player>) {
        val packet = ClientboundSetEquipmentPacket(player.entityId, listOf(Pair.of(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(itemStack))))
        targetPlayer.forEach {
            (it as CraftPlayer).handle.connection.send(packet)
        }
    }

    override fun changePosition(player: Player, location: Location) {
        (player as CraftPlayer).handle.connection.send(ClientboundPlayerPositionPacket(
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch,
            RelativeArgument.ALL,
            player.entityId,
            true
        ))
    }

    override fun getGameProfile(player: Player): GameProfile {
        return (player as CraftPlayer).handle.gameProfile
    }

    override fun getProperties(gameProfile: GameProfile): PropertyMap {
        return gameProfile.properties
    }

    override fun createFakeItem(itemStack: ItemStack, location: Location): FakeItem {
        return FakeItemImpl(itemStack, location)
    }

    override fun getChannel(): NMSChannel {
        return nmsChannel
    }
    private class FakeItemImpl(itemStack: ItemStack, location: Location): FakeItem {
        private val world = (location.world as CraftWorld).handle
        private val craftItem = CraftItem(Bukkit.getServer() as CraftServer, ItemEntity(EntityType.ITEM, world).apply {
            item = CraftItemStack.asNMSCopy(itemStack)
            moveTo(
                location.x,
                location.y,
                location.z,
                location.pitch,
                location.yaw
            )
        })
        override fun getItem(): Item {
            return craftItem
        }

        override fun spawn() {
            world.addFreshEntity(craftItem.handle)
        }
    }
    private abstract class VirtualEntityImpl<T: Entity>(
        protected val t: T,
        player: Player,
        location: Location
    ): VirtualEntity {
        protected val connection: ServerGamePacketListenerImpl = (player as CraftPlayer).handle.connection
        init {
            t.moveTo(
                location.x,
                location.y,
                location.z,
                location.yaw,
                location.pitch
            )
            connection.send(ClientboundAddEntityPacket(t))
        }
        override fun remove() {
            connection.send(ClientboundRemoveEntitiesPacket(t.id))
        }

        override fun teleport(location: Location) {
            t.moveTo(
                location.x,
                location.y,
                location.z,
                location.yaw,
                location.pitch
            )
            connection.send(ClientboundTeleportEntityPacket(t))
        }

        protected fun sendEntityDataPacket() {
            t.entityData.nonDefaultValues?.let {
                connection.send(ClientboundSetEntityDataPacket(t.id, it))
            }
        }
    }
    private class VirtualPlayerImpl(player: Player, location: Location, gameProfile: GameProfile): VirtualEntityImpl<ServerPlayer>(
        ServerPlayer((Bukkit.getServer() as CraftServer).server, (location.world as CraftWorld).handle.level, gameProfile).apply {
            entityData.set(ServerPlayer.DATA_PLAYER_MODE_CUSTOMISATION, 127)
        },
        player,
        location
    ), VirtualPlayer {
        init {
            connection.send(ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),listOf(t)))
            connection.send(ClientboundAddPlayerPacket(t))
            sendEntityDataPacket()
        }

        override fun remove() {
            connection.send(ClientboundPlayerInfoRemovePacket(listOf(t.uuid)))
            super.remove()
        }
    }
    private class VirtualArmorStandImpl(player: Player, location: Location): VirtualEntityImpl<ArmorStand>(ArmorStand(EntityType.ARMOR_STAND, (location.world as CraftWorld).handle).apply {
        isInvisible = true
        isCustomNameVisible = true
    }, player, location), VirtualArmorStand {
        override fun setItem(itemStack: ItemStack) {
            val i = CraftItemStack.asNMSCopy(itemStack)
            t.setItemSlot(EquipmentSlot.HEAD, i)
            connection.send(ClientboundSetEquipmentPacket(t.id, listOf(Pair.of(EquipmentSlot.HEAD, i))))
        }
        override fun setText(text: Component) {
            t.customName = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(text))
            sendEntityDataPacket()
        }
        override fun setCustomNameVisible(boolean: Boolean) {
            t.isCustomNameVisible = boolean
            sendEntityDataPacket()
        }
    }
    override fun getEyeHeight(entity: org.bukkit.entity.Entity): Float {
        return (entity as CraftEntity).handle.eyeHeight
    }
}
