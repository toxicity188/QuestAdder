package kor.toxicity.questadder.shop.implement

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kor.toxicity.questadder.api.event.ShopBuyEvent
import kor.toxicity.questadder.api.event.ShopSellEvent
import kor.toxicity.questadder.api.mechanic.DialogSender
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.ShopManager
import kor.toxicity.questadder.shop.blueprint.ShopItemBlueprint
import kor.toxicity.questadder.shop.stock.GlobalShopStockData
import kor.toxicity.questadder.shop.stock.PlayerShopStockData
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.HashMap

class ShopItem(val blueprint: ShopItemBlueprint, jsonObject: JsonObject) {
    private val stockData = if (blueprint.global) {
        GlobalShopStockData(try {
            jsonObject.getAsJsonPrimitive("stock")?.asLong ?: blueprint.stock
        } catch (ex: Exception) {
            blueprint.stock
        })
    } else {
        PlayerShopStockData(
            try {
                HashMap<UUID, Long>().apply {
                    jsonObject.getAsJsonObject("stock")?.entrySet()?.forEach {
                        put(UUID.fromString(it.key), it.value.asLong)
                    }
                }
            } catch (ex: Exception) {
                HashMap()
            },
            blueprint.stock
        )
    }

    fun serialize(): JsonElement {
        return JsonObject().apply {
            if (blueprint.stock > 0) add("stock", stockData.serialize())
        }
    }

    fun buy(player: Player, sender: DialogSender, shop: Shop): Boolean {
        val moneyValue = blueprint.buyPrice.price
        if (moneyValue < 0) {
            ShopManager.messageBuyCannotBuying.createComponent(player)?.let {
                player.sendMessage(it)
            }
            return false
        }
        if (player.getMoney().toInt() < moneyValue) {
            ShopManager.messageBuyNotHaveMoney.createComponent(player, mapOf("\$money" to listOf(moneyValue.toString().asComponent())))?.let {
                player.sendMessage(it)
            }
            return false
        }
        if (blueprint.stock > 0 && stockData.getStock(player) == 0L) {
            ShopManager.messageBuyNotHaveStock.createComponent(player)?.let {
                player.sendMessage(it)
            }
            return false
        }
        val itemPricePair = blueprint.buyPrice.item.map {
            it.item() to it.chance
        }
        itemPricePair.forEach {
            if (player.totalAmount(it.first) < it.first.amount) {
                ShopManager.messageBuyNotHaveItem.createComponent(player, mapOf("\$item" to listOf(it.first.getNameComponent())))?.let { s ->
                    player.sendMessage(s)
                }
                return false
            }
        }
        val i = blueprint.builder()
        val storage = player.storage(i)
        if (storage < i.amount) {
            ShopManager.messageBuyNotHaveStorage.createComponent(player, mapOf("\$storage" to listOf((i.amount - storage).toString().asComponent())))?.let { s ->
                player.sendMessage(s)
            }
            return false
        }
        fun buyTask() {
            player.give(i)
            player.removeMoney(moneyValue.toDouble())
            if (blueprint.stock > 0) stockData.subtractStock(player, 1)
            itemPricePair.forEach {
                if (ThreadLocalRandom.current().nextDouble(100.0) < it.second) {
                    player.take(it.first)
                }
            }
            ShopBuyEvent(player, shop, i).callEvent()
        }
        blueprint.buyPrice.dialog?.let {
            it.start(player, sender)?.let { state ->
                state.addEndTask {
                    buyTask()
                }
            }
            return false
        } ?: buyTask()
        return true
    }

    fun sell(player: Player, sender: DialogSender, shop: Shop): Boolean {
        val moneyValue = blueprint.sellPrice.price
        if (moneyValue < 0) {
            ShopManager.messageSellCannotSelling.createComponent(player)?.let {
                player.sendMessage(it)
            }
            return false
        }
        val item = blueprint.builder()
        if (player.totalAmount(item) == 0) {
            ShopManager.messageSellNotHaveItem.createComponent(player, mapOf("\$item" to listOf(item.getNameComponent())))?.let {
                player.sendMessage(it)
            }
            return false
        }
        val itemPricePair = blueprint.sellPrice.item.map {
            it.item() to it.chance
        }
        itemPricePair.forEach {
            val fir = it.first
            if (player.storage(fir) < fir.amount) {
                ShopManager.messageSellNotHaveStorage.createComponent(player, mapOf("\$item" to listOf(fir.getNameComponent())))?.let { comp ->
                    player.sendMessage(comp)
                }
                return false
            }
        }
        fun sellTask() {
            player.take(item)
            player.addMoney(moneyValue.toDouble())
            if (blueprint.stock > 0) stockData.addStock(player, 1)
            itemPricePair.forEach {
                if (ThreadLocalRandom.current().nextDouble(100.0) < it.second) {
                    player.give(it.first)
                }
            }
            ShopSellEvent(player, shop, item).callEvent()
        }
        blueprint.sellPrice.dialog?.let {
            it.start(player, sender)?.let { state ->
                state.addEndTask {
                    sellTask()
                }
            }
            return false
        } ?: sellTask()
        return true
    }
}
