package kor.toxicity.questadder.util

import kor.toxicity.questadder.api.util.INamedLocation
import kor.toxicity.questadder.extension.colored
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

data class NamedLocation(private val locationKey: String, val material: Material, val customModelData: Int, val name: Component, private val bukkitLocation: Location): INamedLocation {
    companion object {
        fun fromConfig(key: String, section: ConfigurationSection): NamedLocation? {
            val world = section.getString("world")?.let { w ->
                Bukkit.getWorld(w)
            } ?: return null
            return NamedLocation(key,section.getString("material")?.let {
                try {
                    Material.valueOf(it.uppercase())
                } catch (ex: Exception) {
                    null
                }
            } ?: Material.BOOK,section.getInt("custom-model-data"),(section.getString("name") ?: key).colored(), Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                section.getDouble("yaw").toFloat(),
                section.getDouble("pitch").toFloat()
            ))
        }
    }

    override fun toString(): String {
        return PlainTextComponentSerializer.plainText().serialize(name)
    }

    override fun getKey(): String {
        return locationKey
    }
    override fun getLocation(): Location {
        return bukkitLocation
    }
}