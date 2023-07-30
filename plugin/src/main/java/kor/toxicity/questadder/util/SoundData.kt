package kor.toxicity.questadder.util

import kor.toxicity.questadder.extension.findDouble
import kor.toxicity.questadder.extension.findString
import org.bukkit.configuration.ConfigurationSection

data class SoundData(val name: String, val volume: Float, val pitch: Float){
    companion object {
        fun fromString(name: String) = try {
            val split = name.split(' ')
            if (split.size == 3) SoundData(split[0],split[1].toFloat(),split[2].toFloat()) else null
        } catch (ex: Exception) {
            null
        }
        fun fromConfig(section: ConfigurationSection) = section.findString("name","Name")?.let { n ->
            SoundData(n,section.findDouble(1.0,"volume","Volume").toFloat(),section.findDouble(1.0,"pitch","Pitch").toFloat())
        }
    }
}