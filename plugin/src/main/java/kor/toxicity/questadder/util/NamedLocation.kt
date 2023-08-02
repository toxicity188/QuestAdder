package kor.toxicity.questadder.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

data class NamedLocation(val name: String, val location: Location) {
    companion object {
        fun fromConfig(key: String, section: ConfigurationSection): NamedLocation? {
            val world = section.getString("world")?.let { w ->
                Bukkit.getWorld(w)
            } ?: return null
            return NamedLocation(section.getString("name") ?: key, Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                section.getDouble("pitch").toFloat(),
                section.getDouble("yaw").toFloat()
            ))
        }
    }
}