package kor.toxicity.questadder.item

import dev.lone.itemsadder.api.CustomStack
import org.bukkit.inventory.ItemStack

class ItemsAdderItemDataBase: ItemDatabase {
    override fun getItem(name: String): ItemStack? {
        return CustomStack.getInstance(name)?.itemStack
    }

    override fun getKeys(): Collection<String> {
        return CustomStack.getNamespacedIdsInRegistry()
    }

    override fun reload() {
    }

    override fun requiredPlugin(): String {
        return "ItemsAdder"
    }

}