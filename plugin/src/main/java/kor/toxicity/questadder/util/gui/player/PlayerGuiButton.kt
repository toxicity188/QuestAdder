package kor.toxicity.questadder.util.gui.player

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.util.ItemWriter
import org.bukkit.configuration.ConfigurationSection

class PlayerGuiButton<T: Any>(section: ConfigurationSection) {
    val item = section.findConfig("Item","item")?.let {
        ItemWriter<T>(it)
    } ?: throw RuntimeException("item doesn't exist.")
    val slot = section.findStringList("Slot","slot")?.mapNotNull {
        try {
            it.toInt()
        } catch (ex: Exception) {
            QuestAdder.warn("number format error: the value \"$it\" is not an integer.")
            null
        }
    }?.toSet() ?: emptySet()
}