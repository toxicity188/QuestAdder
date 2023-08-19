package kor.toxicity.questadder.item

import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.ItemsAdder
import org.bukkit.inventory.ItemStack

class ItemsAdderItemDataBase: ItemDatabase {
    override fun getItem(name: String): ItemStack? {
        return CustomStack.getInstance(name)?.itemStack
    }

    override fun getKeys(): Collection<String> {
        return ItemsAdder.getAllItems()?.map {
            it.id
        } ?: emptyList()
    }

    override fun getItems(): Collection<ItemPair> {
        return ItemsAdder.getAllItems()?.map {
            ItemPair(it.id,it.itemStack)
        } ?: emptyList()
    }

    override fun reload() {
    }

    override fun requiredPlugin(): String {
        return "ItemsAdder"
    }

}