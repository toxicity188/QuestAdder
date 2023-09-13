package kor.toxicity.questadder.hooker.item

import io.th0rgal.oraxen.api.OraxenItems
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.api.item.ItemPair
import org.bukkit.inventory.ItemStack

class OraxenItemDatabase: ItemDatabase {

    override fun getItem(name: String): ItemStack? {
        return OraxenItems.getItemById(name)?.build()
    }

    override fun getKeys(): Collection<String> {
        return OraxenItems.getEntries().map {
            it.key
        }
    }
    override fun getItems(): Collection<ItemPair> {
        return OraxenItems.getEntries().map {
            ItemPair(it.key,it.value.build())
        }
    }

    override fun reload() {
        OraxenItems.loadItems()
    }

    override fun requiredPlugin(): String {
        return "Oraxen"
    }
}