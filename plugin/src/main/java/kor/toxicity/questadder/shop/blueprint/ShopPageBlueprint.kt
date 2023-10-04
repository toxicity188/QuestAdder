package kor.toxicity.questadder.shop.blueprint

import kor.toxicity.questadder.QuestAdderBukkit
import org.bukkit.configuration.ConfigurationSection

data class ShopPageBlueprint(
    val map: Map<Int, ShopItemBlueprint>
) {
    constructor(adder: QuestAdderBukkit, section: ConfigurationSection): this(
        HashMap<Int, ShopItemBlueprint>().apply {
            section.getConfigurationSection("items")?.let {
                it.getKeys(false).forEach { s ->
                    it.getConfigurationSection(s)?.let { config ->
                        put(s.toInt(),ShopItemBlueprint(adder, config))
                    }
                }
            }
        }.apply {
            if (isEmpty()) throw RuntimeException("page is empty.")
        }
    )
}
