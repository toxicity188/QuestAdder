package kor.toxicity.questadder.hooker.item

import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.ItemsAdder
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.api.item.ItemPair
import org.bukkit.inventory.ItemStack

class ItemsAdderItemDatabase: ItemDatabase {
    override fun getItem(name: String): ItemStack? {
        return CustomStack.getInstance(name)?.itemStack
    }

    override fun getKeys(): Collection<String> {
        return ItemsAdder.getAllItems()?.map {
            it.namespacedID
        } ?: emptyList()
    }

    override fun getItems(): Collection<ItemPair> {
        return ItemsAdder.getAllItems()?.map {
            ItemPair(it.namespacedID,it.itemStack)
        } ?: emptyList()
    }

    override fun reload() {
    }

    override fun requiredPlugin(): String {
        return "ItemsAdder"
    }

}