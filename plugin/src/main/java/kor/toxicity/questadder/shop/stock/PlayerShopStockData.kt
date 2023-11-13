package kor.toxicity.questadder.shop.stock

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kor.toxicity.questadder.QuestAdderBukkit
import org.bukkit.entity.Player
import java.util.UUID

class PlayerShopStockData(private val map: MutableMap<UUID, Long>, private val defaultStock: Long, regenTime: Long): ShopStockData {

    private val task = if (regenTime > 0) QuestAdderBukkit.asyncTaskTimer(regenTime, regenTime) {
        map.entries.forEach {
            it.setValue(it.value + 1)
        }
    } else null

    override fun getStock(player: Player): Long {
        return map[player.uniqueId] ?: defaultStock
    }

    override fun addStock(player: Player, stock: Long) {
        map[player.uniqueId] = getStock(player) + stock
    }

    override fun subtractStock(player: Player, stock: Long) {
        map[player.uniqueId] = getStock(player) - stock
    }

    override fun serialize(): JsonElement {
        return JsonObject().apply {
            map.forEach {
                addProperty(it.key.toString(), it.value)
            }
        }
    }

    override fun cancel() {
        task?.cancel()
    }
}
