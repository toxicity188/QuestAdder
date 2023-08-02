package kor.toxicity.questadder.util.database

import org.bukkit.configuration.ConfigurationSection

interface DatabaseSupplier {
    fun supply(section: ConfigurationSection): Database
}