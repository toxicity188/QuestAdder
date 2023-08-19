package kor.toxicity.questadder.item

import org.bukkit.inventory.ItemStack

interface ItemDatabase {
    fun getKeys(): Collection<String>
    fun getItem(name: String): ItemStack?
    fun getItems(): Collection<ItemPair>
    fun reload()
    fun requiredPlugin(): String
}