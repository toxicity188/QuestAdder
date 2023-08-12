package kor.toxicity.questadder.util

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.GiveRewardEvent
import kor.toxicity.questadder.extension.addMoney
import kor.toxicity.questadder.extension.give
import kor.toxicity.questadder.extension.storage
import kor.toxicity.questadder.manager.ItemManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RewardSet(section: ConfigurationSection) {
    val money = section.getDouble("money").coerceAtLeast(0.0)
    val exp = section.getDouble("exp").coerceAtLeast(0.0)
    val items = section.getConfigurationSection("items")?.run {
        getKeys(false).mapNotNull {
            getConfigurationSection(it)?.let { config ->
                try {
                    RewardSetItem(config)
                } catch (ex: Exception) {
                    QuestAdder.warn("an error has occurred. $it")
                    QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    null
                }
            }
        }
    }?.toTypedArray() ?: emptyArray()

    private val itemAmount = HashMap<ItemStack,Int>().apply {
        for (item in items) {
            val clone = item.item.clone().apply {
                amount = 1
            }
            put(clone,(get(clone) ?: 0) + item.item.amount)
        }
    }


    class RewardSetItem internal constructor(section: ConfigurationSection) {
        val item = section.getString("item")?.let {
            ItemManager.getItem(it)
        } ?: throw RuntimeException("the reward item doesn't exist.")
        val chance = section.getDouble("chance").coerceAtLeast(0.0).coerceAtMost(100.0)
    }

    fun give(player: Player): GiveRewardEvent {
        val event = GiveRewardEvent(player,this).apply {
            callEvent()
        }
        if (!event.isCancelled) {
            player.addMoney(event.money)
            for (itemStack in event.itemStacks) {
                player.give(itemStack)
            }
        }
        return event
    }
    fun isReady(player: Player) = itemAmount.all {
        player.storage(it.key) >= it.value
    }
}