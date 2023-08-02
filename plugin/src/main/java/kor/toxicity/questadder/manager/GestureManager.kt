package kor.toxicity.questadder.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.ticxo.playeranimator.api.model.player.PlayerModel
import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.send
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID

object GestureManager: QuestAdderManager {
    private val gestureMap = HashMap<UUID,QuestPlayerModel>()
    override fun start(adder: QuestAdder) {

    }

    override fun reload(adder: QuestAdder) {
        File(adder.dataFolder.apply {
            mkdir()
        },"gestures").run {
            mkdir()
            listFiles()?.forEach {
                if (it.extension == "bbmodel") {
                    QuestAdder.animator.animationManager.importAnimations("questadder",it)
                }
            }
        }
        Bukkit.getConsoleSender().send("${QuestAdder.animator.animationManager.registry.size} of gestures has successfully loaded.")
    }

    override fun end(adder: QuestAdder) {
    }

    fun play(player: Player, string: String, npc: NPC) {
        val entity = npc.entity as? Player ?: return
        try {
            gestureMap.put(player.uniqueId, object : QuestPlayerModel(entity) {
                override fun spawn() {
                    spawn(player)
                    QuestAdder.nms.removePlayer(player,entity)
                }

                override fun despawn() {
                    despawn(player)
                    QuestAdder.task {
                        ProtocolLibrary.getProtocolManager().updateEntity(entity, listOf(player))
                    }
                    gestureMap.remove(player.uniqueId)
                }

                override fun cancel() {
                    despawn(player)
                }
            }.apply {
                playAnimation("questadder.$string")
            })?.cancel()
        } catch (ex: Exception) {
            ex.printStackTrace()
            QuestAdder.warn("runtime error: unable to load gesture. (${npc.name})")
        }
    }

    private abstract class QuestPlayerModel(player: Player): PlayerModel(player) {
        abstract fun cancel()
    }
}