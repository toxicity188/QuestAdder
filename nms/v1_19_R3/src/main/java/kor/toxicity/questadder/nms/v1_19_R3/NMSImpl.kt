package kor.toxicity.questadder.nms.v1_19_R3

import com.mojang.math.Transformation
import kor.toxicity.questadder.nms.NMS
import kor.toxicity.questadder.nms.RuntimeCommand
import kor.toxicity.questadder.nms.VirtualTextDisplay
import kor.toxicity.questadder.nms.v1_19_R3.crazy_advancements.advancement.ToastNotification
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minecraft.network.protocol.game.*
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityTypes
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_19_R3.CraftServer
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class NMSImpl: NMS {

    override fun removePlayer(player: Player, target: Player) {
        (player as CraftPlayer).handle.b.run {
            a(PacketPlayOutEntityDestroy(target.entityId))
            a(ClientboundPlayerInfoRemovePacket(listOf(target.uniqueId)))
        }
    }

    override fun sendAdvancementMessage(player: Player, component: Component) {
        ToastNotification(
            ItemStack(Material.BOOK),
            LegacyComponentSerializer.legacySection().serialize(component),
            kor.toxicity.questadder.nms.v1_19_R3.crazy_advancements.advancement.AdvancementDisplay.AdvancementFrame.GOAL
        ).send(player)
    }

    override fun createCommand(name: String, executor: CommandExecutor): RuntimeCommand {
        val map = (Bukkit.getServer() as CraftServer).commandMap
        val obj = object : Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                return executor.onCommand(sender,this,commandLabel,args)
            }
        }
        map.register("questadder",obj)
        return object: RuntimeCommand {
            override fun unregister() {
                map.knownCommands.remove("questadder:$name")
                map.knownCommands.remove(name)
            }
        }
    }

    override fun createTextDisplay(player: Player, location: Location): VirtualTextDisplay {
        return VirtualTextDisplayImpl(player, location)
    }
    private class VirtualTextDisplayImpl(player: Player, location: Location): VirtualTextDisplay {
        private val display = Display.TextDisplay(EntityTypes.aX,(location.world as CraftWorld).handle).apply {
            a(location.x,location.y,location.z,location.yaw,location.pitch)
            c(0)
            a(Display.BillboardConstraints.d)
        }
        private val connection = (player as CraftPlayer).handle.b.apply {
            a(PacketPlayOutSpawnEntity(display))
        }

        override fun getUUID(): UUID {
            return display.cs()
        }
        override fun teleport(location: Location) {
            display.a(location.x,location.y,location.z,location.yaw,location.pitch)
            connection.a(PacketPlayOutEntityTeleport(display))
        }
        override fun remove() {
            connection.a(PacketPlayOutEntityDestroy(display.af()))
        }
        override fun setTransform(x: Float, y: Float, z: Float, pitch: Double, yaw: Double, roll: Double, scale: Float) {

            val qx = sin(roll/2) * cos(pitch/2) * cos(yaw/2) - cos(roll/2) * sin(pitch/2) * sin(yaw/2)
            val qy = cos(roll/2) * sin(pitch/2) * cos(yaw/2) + sin(roll/2) * cos(pitch/2) * sin(yaw/2)
            val qz = cos(roll/2) * cos(pitch/2) * sin(yaw/2) - sin(roll/2) * sin(pitch/2) * cos(yaw/2)
            val qw = cos(roll/2) * cos(pitch/2) * cos(yaw/2) + sin(roll/2) * sin(pitch/2) * sin(yaw/2)

            display.a(Transformation(Vector3f(x,y,z), Quaternionf(qx,qy,qz,qw), if (scale != 1F) Vector3f(scale,scale,scale) else null,null))
            connection.a(PacketPlayOutEntityMetadata(display.af(),display.aj().c()))

        }
        override fun setText(text: Component) {
            (display.bukkitEntity as TextDisplay).text(text)
            connection.a(PacketPlayOutEntityMetadata(display.af(),display.aj().b()))
        }
        override fun setOpacity(byte: Byte) {
            display.c(byte)
            connection.a(PacketPlayOutEntityMetadata(display.af(),display.aj().b()))
        }
    }
}