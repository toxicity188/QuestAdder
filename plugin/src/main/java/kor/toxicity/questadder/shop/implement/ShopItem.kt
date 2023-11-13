package kor.toxicity.questadder.shop.implement

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kor.toxicity.questadder.QuestAdderBukkit
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.HashMap

class ShopItem(val blueprint: ShopItemBlueprint, jsonObject: JsonObject) {
    private val stockData = if (blueprint.global) {
        GlobalShopStockData(try {
            jsonObject.getAsJsonPrimitive("stock")?.asLong ?: blueprint.stock
        } catch (ex: Exception) {
            blueprint.stock
        }, blueprint.regenTime)
    } else {
        PlayerShopStockData(
            try {
                ConcurrentHashMap<UUID, Long>().apply {
                    jsonObject.getAsJsonObject("stock")?.entrySet()?.forEach {
                        put(UUID.fromString(it.key), it.value.asLong)
                    }
                }
            } catch (ex: Exception) {
                ConcurrentHashMap()
            },
            blueprint.stock,
            blueprint.regenTime
        )
    }
    private val shopTask = if (blueprint.tick > 0) QuestAdderBukkit.asyncTaskTimer(blueprint.tick, blueprint.tick) {
        if (ThreadLocalRandom.current().nextDouble(100.0) <= blueprint.tickChance) {
            sellRandomNumber = ThreadLocalRandom.current().nextDouble()
            buyRandomNumber = ThreadLocalRandom.current().nextDouble()
        }
    } else null

    var sellRandomNumber = ThreadLocalRandom.current().nextDouble()
        private set
    var buyRandomNumber = ThreadLocalRandom.current().nextDouble()
        private set

    var totalBuy = try {
        jsonObject.getAsJsonPrimitive("total-buy")?.asInt ?: 0
    } catch (ex: Exception) {
        0
    }
        private set
    var totalSell = try {
        jsonObject.getAsJsonPrimitive("total-sell")?.asInt ?: 0
    } catch (ex: Exception) {
        0
    }
        private set

    fun serialize(): JsonElement {
        return JsonObject().apply {
            if (blueprint.stock > 0) add("stock", stockData.serialize())
            if (totalBuy > 0) addProperty("total-buy", totalBuy)
            if (totalSell > 0) addProperty("total-sell", totalSell)
        }
    }

    fun cancel() {
        shopTask?.cancel()
        stockData.cancel()
    }

    fun getStock(player: Player): Long {
        return stockData.getStock(player)
    }

    fun buy(player: Player, sender: DialogSender, shop: Shop, buyAmount: Int = 1): Boolean {
        val moneyValue = blueprint.buyPrice.equation.buyEvaluate(this, player) * buyAmount
        if (moneyValue < 0) {
            ShopManager.messageBuyCannotBuying.createComponent(player)?.let {
                QuestAdderBukkit.audience.player(player).sendMessage(it)
            }
            return false
        }
        if (player.getMoney().toInt() < moneyValue) {
            ShopManager.messageBuyNotHaveMoney.createComponent(player, mapOf("\$money" to listOf(moneyValue.toString().asComponent())))?.let {
                QuestAdderBukkit.audience.player(player).sendMessage(it)
            }
            return false
        }
        if (blueprint.stock > 0 && stockData.getStock(player) < buyAmount) {
            ShopManager.messageBuyNotHaveStock.createComponent(player)?.let {
                QuestAdderBukkit.audience.player(player).sendMessage(it)
            }
            return false
        }
        val itemPricePair = blueprint.buyPrice.item.map {
            it.item().apply {
                amount *= buyAmount
            } to it.chance
        }
        itemPricePair.forEach {
            if (player.totalAmount(it.first) < it.first.amount) {
                ShopManager.messageBuyNotHaveItem.createComponent(player, mapOf("\$item" to listOf(it.first.getNameComponent())))?.let { s ->
                    QuestAdderBukkit.audience.player(player).sendMessage(s)
                }
                return false
            }
        }
        val i = blueprint.builder().apply {
            amount *= buyAmount
        }
        val storage = player.storage(i) * buyAmount
        if (storage < i.amount) {
            ShopManager.messageBuyNotHaveStorage.createComponent(player, mapOf("\$storage" to listOf((i.amount - storage).toString().asComponent())))?.let { s ->
                QuestAdderBukkit.audience.player(player).sendMessage(s)
            }
            return false
        }
        fun buyTask() {
            player.give(i)
            player.removeMoney(moneyValue)
            if (blueprint.stock > 0) stockData.subtractStock(player, buyAmount.toLong())
            itemPricePair.forEach {
                if (ThreadLocalRandom.current().nextDouble(100.0) < it.second) {
                    player.take(it.first)
                }
            }
            totalBuy += buyAmount
            ShopBuyEvent(player, shop, i).call()
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

    fun sell(player: Player, sender: DialogSender, shop: Shop, sellAmount: Int = 1): Boolean {
        val moneyValue = blueprint.sellPrice.equation.sellEvaluate(this, player) * sellAmount
        if (moneyValue < 0) {
            ShopManager.messageSellCannotSelling.createComponent(player)?.let {
                QuestAdderBukkit.audience.player(player).sendMessage(it)
            }
            return false
        }
        val item = blueprint.builder().apply {
            amount *= sellAmount
        }
        if (player.totalAmount(item) < sellAmount) {
            ShopManager.messageSellNotHaveItem.createComponent(player, mapOf("\$item" to listOf(item.getNameComponent())))?.let {
                QuestAdderBukkit.audience.player(player).sendMessage(it)
            }
            return false
        }
        val itemPricePair = blueprint.sellPrice.item.map {
            it.item().apply {
                amount *= sellAmount
            } to it.chance
        }
        itemPricePair.forEach {
            val fir = it.first
            if (player.storage(fir) < fir.amount) {
                ShopManager.messageSellNotHaveStorage.createComponent(player, mapOf("\$item" to listOf(fir.getNameComponent())))?.let { comp ->
                    QuestAdderBukkit.audience.player(player).sendMessage(comp)
                }
                return false
            }
        }
        fun sellTask() {
            player.take(item)
            player.addMoney(moneyValue)
            if (blueprint.stock > 0) stockData.addStock(player, sellAmount.toLong())
            itemPricePair.forEach {
                if (ThreadLocalRandom.current().nextDouble(100.0) < it.second) {
                    player.give(it.first)
                }
            }
            totalSell += sellAmount
            ShopSellEvent(player, shop, item).call()
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
