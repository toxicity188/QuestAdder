package kor.toxicity.questadder.item

import org.bukkit.inventory.ItemStack

interface ItemDatabase {
    fun getItem(name: String): ItemStack?
    fun getKeys(): Collection<String>
    fun reload()
    fun requiredPlugin(): String
}