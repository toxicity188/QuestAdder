package kor.toxicity.questadder.manager

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.RegionEnterEvent
import kor.toxicity.questadder.event.RegionExitEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object WorldGuardManager: QuestAdderManager {

    private val userThreadMap = ConcurrentHashMap<UUID,UserThread>()

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                userThreadMap[player.uniqueId] = UserThread(player)
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                userThreadMap.remove(e.player.uniqueId)?.cancel()
            }
        },adder)
    }

    override fun reload(adder: QuestAdder) {
    }

    override fun end(adder: QuestAdder) {
    }


    private class UserThread(val player: Player) {

        private val platform = WorldGuard.getInstance().platform
        private var oldSet: Set<ProtectedRegion> = emptySet()

        val task = QuestAdder.asyncTaskTimer(5,5) {
            val location = player.location
            val newSet = platform.regionContainer.get(BukkitAdapter.adapt(player.world))?.getApplicableRegions(
                BlockVector3.at(location.blockX,location.blockY,location.blockZ))?.toSet() ?: emptySet()

            val enterSet = HashSet<ProtectedRegion>().apply {
                newSet.forEach {
                    if (!oldSet.contains(it)) add(it)
                }
            }
            val exitSet = HashSet<ProtectedRegion>().apply {
                oldSet.forEach {
                    if (!newSet.contains(it)) add(it)
                }
            }
            if (enterSet.isNotEmpty() && exitSet.isNotEmpty()) QuestAdder.task {
                enterSet.forEach {
                    RegionEnterEvent(player,it).callEvent()
                }
                exitSet.forEach {
                    RegionExitEvent(player,it).callEvent()
                }
            }
        }

        fun cancel() {
            task.cancel()
        }
    }
}