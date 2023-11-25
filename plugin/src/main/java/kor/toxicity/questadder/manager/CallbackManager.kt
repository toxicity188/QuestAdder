package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.info
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

object CallbackManager: QuestAdderManager {
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
    }
    fun openChat(player: Player, callback: (String) -> Unit) {
        chatMap[player.uniqueId] = callback
    }
    fun openSign(player: Player, array: List<Component>, callback: (Array<String>) -> Unit) {
        QuestAdderBukkit.nms.getChannel().openSign(player, array, callback)
    }

    override fun reload(adder: QuestAdderBukkit) {
    }

    override fun end(adder: QuestAdderBukkit) {
    }
}
