package kor.toxicity.questadder.util

import kor.toxicity.questadder.extension.findDouble
import kor.toxicity.questadder.extension.findString
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

data class SoundData(val name: String, val volume: Float, val pitch: Float){
    companion object {
        fun fromString(name: String) = try {
            val split = name.split(' ')
            if (split.isEmpty()) null else when (split.size) {
                1 -> SoundData(split[0],1F,1F)
                2 -> SoundData(split[0],split[1].toFloat(),1F)
                else -> SoundData(split[0],split[1].toFloat(),split[2].toFloat())
            }
        } catch (ex: Exception) {
            null
        }
        fun fromConfig(section: ConfigurationSection) = section.findString("name","Name")?.let { n ->
            SoundData(n,section.findDouble(1.0,"volume","Volume").toFloat(),section.findDouble(1.0,"pitch","Pitch").toFloat())
        }
    }

    fun play(player: Player) {
        player.playSound(player.location,name,volume,pitch)
    }
}