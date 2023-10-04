package kor.toxicity.questadder.shop.implement

import com.google.gson.JsonObject
import kor.toxicity.questadder.shop.blueprint.ShopPageBlueprint

class ShopPage(val blueprint: ShopPageBlueprint, jsonObject: JsonObject) {
    val shopItem: Map<Int, ShopItem> = try {
        HashMap<Int, ShopItem>().apply {
            val items = jsonObject.getAsJsonObject("items") ?: JsonObject()
            blueprint.map.forEach {
                put(it.key, ShopItem(it.value, items.getAsJsonObject(it.key.toString()) ?: JsonObject()))
            }
        }
    } catch (ex: Exception) {
        HashMap<Int, ShopItem>().apply {
            blueprint.map.forEach {
                put(it.key, ShopItem(it.value, JsonObject()))
            }
        }
    }

    fun serialize() = JsonObject().apply {
        add("items", JsonObject().apply {
            shopItem.forEach {
                add(it.key.toString(), it.value.serialize())
            }
        })
    }
}
