package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.manager.registry.EntityRegistry
import kor.toxicity.questadder.nms.VirtualEntity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object EntityManager: QuestAdderManager {
    private val registry = EntityRegistry()
    override fun start(adder: QuestAdderBukkit) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                registry.remove(e.player)
            }
        },adder)
    }

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
        registry.removeAll()
    }

    override fun end(adder: QuestAdderBukkit) {
    }

    fun register(player: Player, key: String, entity: VirtualEntity) = registry.register(player, key, entity)
    fun unregister(player: Player, key: String) = registry.remove(player, key)
    fun unregister(player: Player) = registry.remove(player)
}
