package kor.toxicity.questadder.nms

import org.bukkit.inventory.ItemStack

interface ItemContainer {
    fun setItem(itemStack: ItemStack)
}