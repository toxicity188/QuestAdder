package kor.toxicity.questadder.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import kor.toxicity.questadder.QuestAdderBukkit
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

object CallbackManager: QuestAdderManager {
    private val signMap = HashMap<UUID,(Array<String>) -> Unit>()
    override fun start(adder: QuestAdderBukkit) {
        ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(adder, PacketType.Play.Client.UPDATE_SIGN) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                signMap.remove(player.uniqueId)?.let {
                    it(event.packet.stringArrays.read(0))
                }
            }
        })
    }
    fun openSign(player: Player, array: List<Component>, callback: (Array<String>) -> Unit) {
        val loc = player.location.apply {
            y = 0.0
        }
        ProtocolLibrary.getProtocolManager().run {

            val position = BlockPosition(loc.blockX,loc.blockY,loc.blockZ)

            sendServerPacket(player, createPacket(PacketType.Play.Server.BLOCK_CHANGE).apply {
                blockData.write(0,WrappedBlockData.createData(Material.OAK_SIGN))
                blockPositionModifier.write(0,position)
            })
            QuestAdderBukkit.platform.changeSign(player, loc, array)
            sendServerPacket(player, createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR).apply {
                blockPositionModifier.write(0,position)
            })
            signMap[player.uniqueId] = {
                QuestAdderBukkit.task {
                    callback(it)
                    sendServerPacket(player, createPacket(PacketType.Play.Server.BLOCK_CHANGE).apply {
                        blockData.write(0, WrappedBlockData.createData(Material.AIR))
                        blockPositionModifier.write(0,position)
                    })
                }
            }
        }
    }

    override fun reload(adder: QuestAdderBukkit) {
    }

    override fun end(adder: QuestAdderBukkit) {
    }
}
