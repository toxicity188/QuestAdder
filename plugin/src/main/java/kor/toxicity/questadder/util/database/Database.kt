package kor.toxicity.questadder.util.database

import com.google.gson.JsonObject
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.shop.blueprint.ShopBlueprint
import kor.toxicity.questadder.shop.implement.Shop
import org.bukkit.OfflinePlayer

interface Database {
    fun load(adder: QuestAdderBukkit, player: OfflinePlayer): PlayerData
    fun save(adder: QuestAdderBukkit, player: OfflinePlayer, playerData: PlayerData): Boolean
    fun close()
    fun loadShop(adder: QuestAdderBukkit, blueprint: ShopBlueprint): JsonObject
    fun saveShop(adder: QuestAdderBukkit, shop: Shop): Boolean
}
