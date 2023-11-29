package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.NavigateCompleteEvent
import kor.toxicity.questadder.api.util.INamedLocation
import kor.toxicity.questadder.extension.WHITE
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.call
import kor.toxicity.questadder.extension.rotateYaw
import kor.toxicity.questadder.nms.TextContainer
import kor.toxicity.questadder.nms.VirtualEntity
import kor.toxicity.questadder.nms.VirtualItemDisplay
import kor.toxicity.questadder.nms.VirtualTextDisplay
import kor.toxicity.questadder.util.NamedLocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.text.DecimalFormat
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2

object NavigationManager: QuestAdderManager {

    private val threadMap = ConcurrentHashMap<UUID, NavigationThread>()

    private fun degree(playerLocation: Location, location: Location): Double {
        var degree = Math.toDegrees(atan2(location.z - playerLocation.z, location.x - playerLocation.x))
        if (degree < 0) degree += 360.0
        if (degree > 360) degree -= 360
        return degree
    }

    fun startNavigate(player: Player, location: INamedLocation) {
        if (player.world != location.location.world) return
        threadMap.put(player.uniqueId, NavigationThread(player,location))?.cancel()
    }
    fun onNavigate(player: Player) = threadMap.containsKey(player.uniqueId)
    fun endNavigate(player: Player) {
        threadMap.remove(player.uniqueId)?.cancel()
    }

    private class NavigationThread(private val player: Player, val destination: INamedLocation) {
        val destinationLocation = destination.location
        private val initialLocation = player.location
        private val display: VirtualEntity = try {
            QuestAdderBukkit.nms.createItemDisplay(player,initialLocation)
        } catch (ex: Exception) {
            QuestAdderBukkit.nms.createArmorStand(player,initialLocation).apply {
                setText(Component.empty())
            }
        }.apply {
            setItem(ItemStack(QuestAdderBukkit.Config.defaultResourcePackItem).apply {
                itemMeta = itemMeta?.apply {
                    setCustomModelData(1)
                }
            })
        }
        private val text: VirtualEntity = try {
            QuestAdderBukkit.nms.createTextDisplay(player,initialLocation)
        } catch (ex: Exception) {
            QuestAdderBukkit.nms.createArmorStand(player,initialLocation)
        }

        private val textVector = if (text is VirtualTextDisplay) 0.4 else -1.6

        private val pillar = try {
            QuestAdderBukkit.nms.createItemDisplay(player,destinationLocation.clone().apply {
                pitch = 0F
                yaw = 0F
            }).apply {
                setItem(ItemStack(QuestAdderBukkit.Config.defaultResourcePackItem).apply {
                    itemMeta = itemMeta?.apply {
                        setCustomModelData(2)
                    }
                })
                setSize(1.0,64.0,1.0)
            }
        } catch (ex: Exception) {
            null
        }

        private val vector = if (display is VirtualItemDisplay) 0.6 else -1.4
        private val yawAdd = if (display is VirtualItemDisplay) 90F else 0F
        private val maxDistance = destinationLocation.distance(initialLocation)

        init {
            update()
        }
        fun update() {
            val loc = player.location
            val degree = degree(loc,destinationLocation)
            val dist = loc.distance(destinationLocation)
            val distance = (dist / maxDistance).coerceAtMost(1.0)
            (text as TextContainer).setText("${DecimalFormat.getInstance().format(dist)}m".asComponent().color(WHITE).decorate(TextDecoration.BOLD))
            text.teleport(loc.clone().add(Vector(2.5,0.0,0.0).rotateYaw(Math.toRadians(degree))).apply {
                y += textVector
                pitch = 0F
            })
            display.teleport(loc.clone().add(Vector(2.0 + 1.5 * distance,0.0,0.0).rotateYaw(Math.toRadians(degree))).apply {
                y += vector
                pitch = 0F
                yaw = degree.toFloat() + yawAdd
            })
            if (display is VirtualItemDisplay) {
                display.setSize(1.0,1.0,0.5 + distance * 1.5)
            }
        }
        fun cancel() {
            text.remove()
            display.remove()
            pillar?.remove()
        }
    }

    override fun start(adder: QuestAdderBukkit) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                endNavigate(e.player)
            }
            @EventHandler
            fun move(e: PlayerMoveEvent) {
                val player = e.player
                threadMap[player.uniqueId]?.let {
                    if (player.location.distance(it.destinationLocation) < 3) {
                        it.cancel()
                        threadMap.remove(player.uniqueId)
                        NavigateCompleteEvent(
                            player,
                            it.destination
                        ).call()
                    } else it.update()
                }
            }
            @EventHandler
            fun change(e: PlayerChangedWorldEvent) {
                endNavigate(e.player)
            }
            @EventHandler
            fun death(e: PlayerDeathEvent) {
                endNavigate(e.entity)
            }
        },adder)
    }

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
    }

    override fun end(adder: QuestAdderBukkit) {
    }
}
