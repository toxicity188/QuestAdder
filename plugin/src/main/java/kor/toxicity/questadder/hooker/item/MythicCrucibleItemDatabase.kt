package kor.toxicity.questadder.hooker.item

import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythiccrucible.MythicCrucible
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.api.item.ItemPair
import org.bukkit.inventory.ItemStack

class MythicCrucibleItemDatabase: ItemDatabase {
    override fun getKeys(): MutableCollection<String> {
        return MythicCrucible.inst().itemManager.itemNames
    }

    override fun getItem(name: String): ItemStack? {
        return MythicCrucible.inst().itemManager.getItem(name).map {
            BukkitAdapter.adapt(it.mythicItem.generateItemStack(1))
        }.orElse(null)
    }

    override fun getItems(): Collection<ItemPair> {
        return MythicCrucible.inst().itemManager.items.map {
            ItemPair(it.internalName, BukkitAdapter.adapt(it.mythicItem.generateItemStack(1)))
        }
    }

    override fun reload() {
        MythicCrucible.inst().itemManager.doReload()
    }

    override fun requiredPlugin(): String {
        return "MythicCrucible"
    }
}
