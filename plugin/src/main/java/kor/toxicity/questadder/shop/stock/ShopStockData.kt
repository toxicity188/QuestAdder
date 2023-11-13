package kor.toxicity.questadder.shop.stock

import com.google.gson.JsonElement
import org.bukkit.entity.Player

interface ShopStockData {
    fun getStock(player: Player): Long
    fun addStock(player: Player, stock: Long)
    fun subtractStock(player: Player, stock: Long)

    fun serialize(): JsonElement
    fun cancel()
}
