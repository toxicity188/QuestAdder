package kor.toxicity.questadder.util.database

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.extension.getAsStringList
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

enum class StandardDatabaseSupplier: DatabaseSupplier {
    YML {
        override fun supply(section: ConfigurationSection): Database {
            return object : Database {
                private fun getFile(adder: QuestAdder, player: OfflinePlayer) = File(File(adder.dataFolder.apply {
                    mkdir()
                },"users").apply {
                    mkdir()
                },"${player.uniqueId}.yml")

                override fun load(adder: QuestAdder, player: OfflinePlayer): PlayerData {
                    val data = PlayerData()
                    try {
                        val collection = ArrayList<Triple<String, String, String>>()
                        YamlConfiguration().run {
                            load(getFile(adder,player))
                            getAsStringList("variables")?.forEach {
                                val split = it.split(',')
                                if (split.size == 3) collection.add(Triple(split[0], split[1], split[2]))
                            }
                        }
                        data.loadVariables(collection)
                    } catch (ex: Exception) {
                        QuestAdder.warn("unable to load the player data of ${player.uniqueId}.")
                    }
                    return data
                }

                override fun save(adder: QuestAdder, player: OfflinePlayer, playerData: PlayerData): Boolean {
                    val save = playerData.saveVariables()
                    return try {
                        YamlConfiguration().run {
                            set("variables",save.map {
                                "${it.first},${it.second},${it.third}"
                            }.toTypedArray())
                            save(getFile(adder,player))
                        }
                        true
                    } catch (ex: Exception) {
                        false
                    }
                }
            }
        }
    },
    MYSQL {
        override fun supply(section: ConfigurationSection): Database {
            TODO("Not yet implemented")
        }
    }
}