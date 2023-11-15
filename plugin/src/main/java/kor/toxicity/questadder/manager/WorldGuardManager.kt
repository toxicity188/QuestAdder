package kor.toxicity.questadder.manager

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.RegionEnterEvent
import kor.toxicity.questadder.api.event.RegionExitEvent
import kor.toxicity.questadder.extension.call
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

    override fun start(adder: QuestAdderBukkit) {
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

    override fun reload(adder: QuestAdderBukkit) {
    }

    override fun end(adder: QuestAdderBukkit) {
    }


    private class UserThread(val player: Player) {

        private val platform = WorldGuard.getInstance().platform
        private var oldSet: Set<ProtectedRegion> = emptySet()

        val task = QuestAdderBukkit.asyncTaskTimer(5,5) {
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
            if (enterSet.isNotEmpty() || exitSet.isNotEmpty()) QuestAdderBukkit.task {
                enterSet.forEach {
                    RegionEnterEvent(player, it).call()
                }
                exitSet.forEach {
                    RegionExitEvent(player, it).call()
                }
            }
        }

        fun cancel() {
            task.cancel()
        }
    }
}
