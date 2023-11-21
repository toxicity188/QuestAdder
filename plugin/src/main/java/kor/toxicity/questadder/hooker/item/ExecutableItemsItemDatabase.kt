package kor.toxicity.questadder.hooker.item

import com.ssomar.score.api.executableitems.ExecutableItemsAPI
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.api.item.ItemPair
import org.bukkit.inventory.ItemStack
import java.util.*

class ExecutableItemsItemDatabase: ItemDatabase {
    private val manager = ExecutableItemsAPI.getExecutableItemsManager()
    override fun getKeys(): Collection<String> {
        return manager.executableItemIdsList
    }

    override fun getItem(name: String): ItemStack? {
        return manager.getExecutableItem(name).map {
            it.buildItem(1, Optional.empty(), Optional.empty())
        }.orElse(null)
    }

    override fun getItems(): Collection<ItemPair> {
        return manager.executableItemIdsList.mapNotNull {
            ItemPair(it, manager.getExecutableItem(it).map { ei ->
                ei.buildItem(1, Optional.empty(), Optional.empty())
            }.orElse(null))
        }
    }

    override fun reload() {

    }

    override fun requiredPlugin(): String {
        return "ExecutableItems"
    }
}
