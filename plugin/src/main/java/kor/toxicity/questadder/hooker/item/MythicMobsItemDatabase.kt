package kor.toxicity.questadder.hooker.item

import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.api.item.ItemPair
import org.bukkit.inventory.ItemStack

class MythicMobsItemDatabase: ItemDatabase {
    override fun getKeys(): Collection<String> {
        return MythicBukkit.inst().itemManager.itemGroupNames
    }

    override fun getItem(name: String): ItemStack? {
        return MythicBukkit.inst().itemManager.getItemStack(name)
    }

    override fun getItems(): Collection<ItemPair> {
        return MythicBukkit.inst().itemManager.items.map {
            ItemPair(it.internalName, BukkitAdapter.adapt(it.generateItemStack(1)))
        }
    }

    override fun reload() {
        MythicBukkit.inst().itemManager.reload()
    }

    override fun requiredPlugin(): String {
        return "MythicMobs"
    }

}
