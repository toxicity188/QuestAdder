package kor.toxicity.questadder.util.gui.player

import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.util.ItemWriter
import org.bukkit.configuration.ConfigurationSection

class PlayerGuiButton<T: Any>(section: ConfigurationSection) {
    val item = section.findConfig("Item","item")?.let {
        ItemWriter<T>(it)
    } ?: throw RuntimeException("item doesn't exist.")
    val slot = section.findStringList("Slot","slot")?.map {
        it.toInt()
    }?.toSet() ?: emptySet()
}