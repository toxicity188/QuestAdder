package kor.toxicity.questadder.manager.registry

import kor.toxicity.questadder.nms.VirtualEntity
import org.bukkit.entity.Player
import java.util.UUID

class EntityRegistry {
    private val virtualEntityMap: MutableMap<UUID,MutableMap<String,VirtualEntity>> = HashMap()

    fun register(player: Player, key: String, entity: VirtualEntity) {
        virtualEntityMap.getOrPut(player.uniqueId) {
            HashMap()
        }.put(key,entity)?.remove()
    }

    fun remove(player: Player, key: String) {
        val map = virtualEntityMap[player.uniqueId] ?: return
        map.remove(key)?.remove()
        if (map.isEmpty()) virtualEntityMap.remove(player.uniqueId)
    }

    fun remove(player: Player) {
        virtualEntityMap.remove(player.uniqueId)?.let {
            it.values.forEach { e ->
                e.remove()
            }
        }
    }

    fun removeAll() {
        virtualEntityMap.values.forEach {
            it.values.forEach { e ->
                e.remove()
            }
        }
        virtualEntityMap.clear()
    }
}