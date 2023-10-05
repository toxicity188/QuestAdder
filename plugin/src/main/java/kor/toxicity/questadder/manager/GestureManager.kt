package kor.toxicity.questadder.manager

import com.comphenix.protocol.ProtocolLibrary
import com.ticxo.playeranimator.api.model.player.PlayerModel
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.send
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.util.*

object GestureManager: QuestAdderManager {
    private val gestureMap = HashMap<UUID,QuestPlayerModel>()
    override fun start(adder: QuestAdderBukkit) {

    }

    override fun reload(adder: QuestAdderBukkit) {
        QuestAdderBukkit.animator?.let { animator ->
            File(adder.dataFolder.apply {
                mkdir()
            },"gestures").run {
                mkdir()
                listFiles()?.forEach {
                    if (it.extension == "bbmodel") {
                        animator.animationManager.importAnimations("questadder",it)
                    }
                }
            }
            Bukkit.getConsoleSender().send("${animator.animationManager.registry.size} of gestures has successfully loaded.")
        }
    }

    override fun end(adder: QuestAdderBukkit) {
    }

    fun play(player: Player, string: String, npc: NPC) {
        val entity = npc.entity as? Player ?: return
        try {
            gestureMap.put(player.uniqueId, object : QuestPlayerModel(entity) {
                override fun spawn() {
                    spawn(player)
                    QuestAdderBukkit.nms.removePlayer(player,entity)
                }

                override fun despawn() {
                    despawn(player)
                    QuestAdderBukkit.task {
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
            QuestAdderBukkit.warn("runtime error: unable to load gesture. (${npc.name})")
        }
    }

    private abstract class QuestPlayerModel(player: Player): PlayerModel(player) {
        abstract fun cancel()
    }
}
