package kor.toxicity.questadder.hooker.item

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kor.toxicity.questadder.api.item.ItemPair
import kor.toxicity.questadder.api.item.ItemSupplier
import kor.toxicity.questadder.api.item.JsonItemDatabase
import net.Indyuce.mmoitems.MMOItems
import org.bukkit.inventory.ItemStack

class MMOItemsItemDatabase: JsonItemDatabase {
    override fun getKeys(): Collection<String> {
        val collection = ArrayList<String>()
        MMOItems.plugin.types.all.forEach {
            collection.addAll(MMOItems.plugin.templates.getTemplateNames(it))
        }
        return collection
    }

    override fun getItem(name: String): ItemStack? {
        return MMOItems.plugin.types.all.firstNotNullOfOrNull {
            MMOItems.plugin.getItem(it, name)
        }
    }

    override fun getItems(): Collection<ItemPair> {
        val collection = ArrayList<ItemPair>()
        MMOItems.plugin.types.all.forEach {
            collection.addAll(MMOItems.plugin.templates.getTemplates(it).mapNotNull { t ->
                t.newBuilder().build().newBuilder().build()?.let { item ->
                    ItemPair(t.id,item)
                }
            })
        }
        return collection
    }

    override fun reload() {
    }

    override fun requiredPlugin(): String {
        return "MMOItems"
    }

    override fun getItemSupplier(name: String, jsonObject: JsonObject): ItemSupplier {
        val type = jsonObject.getAsJsonPrimitive("type")?.let {
            MMOItems.plugin.types.get(it.asString)
        } ?: throw RuntimeException("type value not found!")
        val temp = MMOItems.plugin.templates.getTemplate(type, name) ?: throw RuntimeException("template value not found!")
        return ItemSupplier {
            temp.newBuilder(
                jsonObject.getAsJsonPrimitive("level")?.asInt ?: 0,
                jsonObject.getAsJsonPrimitive("tier")?.let {
                    MMOItems.plugin.tiers.get(it.asString)
                }
            ).build().newBuilder().build() ?: throw RuntimeException("item value not found!")
        }
    }
}
