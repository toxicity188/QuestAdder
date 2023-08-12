package kor.toxicity.questadder.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import kor.toxicity.questadder.QuestAdder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object SlateManager: QuestAdderManager {

    private val slateMap = ConcurrentHashMap<UUID,SlateData>()

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            private fun stop(player: Player) {
                slateMap.remove(player.uniqueId)?.cancel()
            }
            @EventHandler
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                if (!player.isOp) {
                    player.isInvisible = false
                }
            }

            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                stop(e.player)
            }
            @EventHandler
            fun move(e: PlayerMoveEvent) {
                if (slateMap.containsKey(e.player.uniqueId)) e.isCancelled = true
            }
            @EventHandler
            fun teleport(e: PlayerTeleportEvent) {
                if (e.cause != PlayerTeleportEvent.TeleportCause.PLUGIN && slateMap.containsKey(e.player.uniqueId)) e.isCancelled = true
            }
            @EventHandler
            fun changeGameMode(e: PlayerGameModeChangeEvent) {
                stop(e.player)
            }
            @EventHandler
            fun click(e: InventoryClickEvent) {
                if (slateMap.containsKey(e.whoClicked.uniqueId)) e.isCancelled = true
            }
            @EventHandler
            fun interact(e: PlayerInteractEvent) {
                if (slateMap.containsKey(e.player.uniqueId)) e.isCancelled = true
            }
        },adder)
        ProtocolLibrary.getProtocolManager().run {
            addPacketListener(object : PacketAdapter(adder,PacketType.Play.Client.HELD_ITEM_SLOT) {
                override fun onPacketReceiving(event: PacketEvent) {
                    if (slateMap.contains(event.player.uniqueId)) event.isCancelled = true
                }
            })
            addPacketListener(object : PacketAdapter(adder,PacketType.Play.Client.ENTITY_ACTION) {
                override fun onPacketReceiving(event: PacketEvent) {
                    if (slateMap.contains(event.player.uniqueId)) event.isCancelled = true
                }
            })
        }
    }

    override fun reload(adder: QuestAdder) {
        QuestAdder.task {
            for (mutableEntry in slateMap) {
                mutableEntry.value.cancel()
            }
            slateMap.clear()
        }
    }

    override fun end(adder: QuestAdder) {
    }

    fun slate(player: Player, back: Boolean = true) {
        if (!slateMap.containsKey(player.uniqueId)) {
            slateOn(player)
        } else {
            slateOff(player,back)
        }
    }
    fun slateOn(player: Player) {
        slateMap[player.uniqueId] = SlateData(player)
    }
    fun slateOff(player: Player, back: Boolean = true) {
        slateMap.remove(player.uniqueId)?.cancel(back)
    }

    private class SlateData(val player: Player) {
        private val location = player.location
        init {
            player.isInvisible = true
        }

        fun cancel(back: Boolean = true) {
            if (back) player.teleport(location)
            player.isInvisible = false
        }
    }
}