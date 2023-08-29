package kor.toxicity.questadder.util

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.api.event.GiveRewardEvent
import kor.toxicity.questadder.api.util.IRewardSet
import kor.toxicity.questadder.api.util.IRewardSetContent
import kor.toxicity.questadder.extension.addMoney
import kor.toxicity.questadder.extension.give
import kor.toxicity.questadder.extension.storage
import kor.toxicity.questadder.manager.ItemManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class RewardSet(section: ConfigurationSection): IRewardSet {
    val rewardSetMoney = section.getDouble("money").coerceAtLeast(0.0)
    val rewardSetExp = section.getDouble("exp").coerceAtLeast(0.0)
    val rewardSetItems = section.getConfigurationSection("items")?.run {
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
        for (item in rewardSetItems) {
            val clone = item.contentItem.clone().apply {
                amount = 1
            }
            put(clone,(get(clone) ?: 0) + item.contentItem.amount)
        }
    }


    class RewardSetItem internal constructor(section: ConfigurationSection): IRewardSetContent {
        val contentItem = section.getString("item")?.let {
            ItemManager.getItem(it)
        } ?: throw RuntimeException("the reward item doesn't exist.")
        val contentChance = section.getDouble("chance").coerceAtLeast(0.0).coerceAtMost(100.0)

        override fun getChance(): Double {
            return contentChance
        }

        override fun getItem(): ItemStack {
            return contentItem
        }
    }

    fun give(player: Player): GiveRewardEvent {
        val event = GiveRewardEvent(player, this).apply {
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

    override fun getExp(): Double {
        return rewardSetExp
    }

    override fun getMoney(): Double {
        return rewardSetMoney
    }

    override fun getItems(): Array<out IRewardSetContent> {
        return rewardSetItems.copyOf()
    }
}