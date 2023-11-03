package kor.toxicity.questadder.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.info
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object CallbackManager: QuestAdderManager {
    private val signMap = HashMap<UUID,(Array<String>) -> Unit>()
    private val chatMap = HashMap<UUID,(String) -> Unit>()
    override fun start(adder: QuestAdderBukkit) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun chat(e: AsyncPlayerChatEvent) {
                val player = e.player
                chatMap.remove(player.uniqueId)?.let {
                    val message = e.message
                    if (message == "cancel") {
                        player.info("your action was cancelled.")
                    } else it(message)
                }
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                chatMap.remove(e.player.uniqueId)
            }
        }, adder)
        ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(adder, PacketType.Play.Client.UPDATE_SIGN) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                signMap.remove(player.uniqueId)?.let {
                    it(event.packet.stringArrays.read(0))
                }
            }
        })
    }
    fun openChat(player: Player, callback: (String) -> Unit) {
        chatMap[player.uniqueId] = callback
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
