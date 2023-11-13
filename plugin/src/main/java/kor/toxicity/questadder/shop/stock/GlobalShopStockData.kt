package kor.toxicity.questadder.shop.stock

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import kor.toxicity.questadder.QuestAdderBukkit
import org.bukkit.entity.Player

class GlobalShopStockData(private var stock: Long, regenTime: Long): ShopStockData {

    private val task = if (regenTime > 0) QuestAdderBukkit.asyncTaskTimer(regenTime, regenTime) {
        stock++
    } else null

    override fun getStock(player: Player): Long {
        return stock
    }

    override fun addStock(player: Player, stock: Long) {
        this.stock += stock
    }

    override fun subtractStock(player: Player, stock: Long) {
        this.stock -= stock
    }

    override fun serialize(): JsonElement {
        return JsonPrimitive(stock)
    }

    override fun cancel() {
        task?.cancel()
    }
}
