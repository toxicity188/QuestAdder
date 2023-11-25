package kor.toxicity.questadder.nms.v1_19_R2

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.manager.SlateManager
import kor.toxicity.questadder.nms.NMSChannel
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.block.Blocks
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

class NMSChannelImpl: NMSChannel {
    private val playerChannelMap = HashMap<UUID, PlayerChannel>()
    private val blockData = Blocks.OAK_SIGN.defaultBlockState()
    private val air = Blocks.AIR.defaultBlockState()
    private val channelField = ServerGamePacketListenerImpl::class.java.declaredFields.first {
        it.type == Connection::class.java
    }
    override fun inject(player: Player) {
        playerChannelMap[player.uniqueId] = PlayerChannel((player as CraftPlayer).handle)
    }

    override fun uninject(player: Player) {
        playerChannelMap.remove(player.uniqueId)?.remove()
    }

    override fun openSign(player: Player, array: List<Component>, callback: (Array<String>) -> Unit) {
        playerChannelMap[player.uniqueId]?.let {
            val originalLocation = player.location.apply {
                y = 255.0
            }
            val blockPos = BlockPos(originalLocation.blockX, 255, originalLocation.blockZ)

            val connection = (player as CraftPlayer).handle.connection

            connection.send(ClientboundBlockUpdatePacket(blockPos, blockData))
            QuestAdderBukkit.platform.changeSign(player, originalLocation, array)
            connection.send(ClientboundOpenSignEditorPacket(blockPos))
            it.signCallback = { arr ->
                connection.send(ClientboundBlockUpdatePacket(blockPos, air))
                QuestAdderBukkit.task {
                    callback(arr)
                }
            }
        }
    }
    private fun getConnection(player: ServerPlayer): Connection {
        channelField.isAccessible = true
        val get = channelField.get(player.connection) as Connection
        channelField.isAccessible = false
        return get
    }

    private inner class PlayerChannel(val player: ServerPlayer): ChannelDuplexHandler() {
        var signCallback: ((Array<String>) -> Unit)? = null

        private val channel = getConnection(player)

        init {
            val pipeLine = channel.channel.pipeline()
            pipeLine.toMap().forEach {
                if (it.value is Connection) {
                    pipeLine.addBefore(it.key, "questadder", this)
                }
            }
        }
        fun remove() {
            val channel = channel.channel
            channel.eventLoop().submit {
                channel.pipeline().remove("questadder")
            }
        }

        override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
            if (SlateManager.isSlated(player.uuid)) {
                when (msg) {
                    is ClientboundSetEquipmentPacket -> return
                    is ClientboundEntityEventPacket -> return
                }
            }
            super.write(ctx, msg, promise)
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is ServerboundSignUpdatePacket) {
                signCallback?.let {
                    it(msg.lines)
                    signCallback = null
                }
            }
            super.channelRead(ctx, msg)
        }
    }
}
