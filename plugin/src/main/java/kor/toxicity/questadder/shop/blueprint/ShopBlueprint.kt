package kor.toxicity.questadder.shop.blueprint

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findInt
import kor.toxicity.questadder.extension.findString
import kor.toxicity.questadder.util.ComponentReader
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File

class ShopBlueprint(
    val id: String,
    val file: File,
    val name: ComponentReader<Player>,
    val size: Int,
    val pages: List<ShopPageBlueprint>
) {
    constructor(adder: QuestAdderBukkit, file: File, id: String, section: ConfigurationSection): this(
        id,
        file,
        section.findString("Name","name")?.let {
            ComponentReader(it)
        } ?: throw RuntimeException("name value doesn't exist."),
        section.findInt(4,"Size","size").coerceAtLeast(1).coerceAtMost(4) + 2,
        ArrayList<ShopPageBlueprint>().apply {
            section.findConfig("Pages","Page","page","pages")?.let {
                it.getKeys(false).forEach { s ->
                    it.getConfigurationSection(s)?.let { config ->
                        add(ShopPageBlueprint(adder, config))
                    }
                }
            }
            if (isEmpty()) throw RuntimeException("shop page is empty.")
        }
    )
}
