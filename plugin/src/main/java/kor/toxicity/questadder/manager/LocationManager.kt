package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.util.NamedLocation
import org.bukkit.Bukkit

object LocationManager: QuestAdderManager {

    private val locationMap = HashMap<String,NamedLocation>()

    override fun start(adder: QuestAdder) {

    }

    override fun reload(adder: QuestAdder) {
        locationMap.clear()
        adder.loadFolder("locations") { file, section ->
            section.getKeys(false).forEach {
                section.getConfigurationSection(it)?.let { c ->
                    NamedLocation.fromConfig(it,c)?.let { location ->
                        locationMap[it] = location
                    } ?: QuestAdder.warn("unable to read this location. ($it in ${file.name})")
                }  ?: QuestAdder.warn("syntax error: the value is not a configuration section. ($it in ${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${locationMap.size} of locations has successfully loaded.")
    }

    override fun end(adder: QuestAdder) {
    }
}