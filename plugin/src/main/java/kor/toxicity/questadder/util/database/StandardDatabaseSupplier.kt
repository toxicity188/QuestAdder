package kor.toxicity.questadder.util.database

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.extension.getAsStringList
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
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
                        val map = HashMap<String,MutableList<Pair<String,Long>>>()
                        YamlConfiguration().run {
                            load(getFile(adder,player))
                            getAsStringList("variables")?.forEach {
                                val split = it.split(',')
                                if (split.size == 3) collection.add(Triple(split[0], split[1], split[2]))
                            }
                            getConfigurationSection("quests")?.let {
                                it.getKeys(false).forEach { s ->
                                    val array = ArrayList<Pair<String,Long>>()
                                    it.getConfigurationSection(s)?.let { config ->
                                        for (key in config.getKeys(false)) {
                                            array.add(key to config.getLong(key))
                                        }
                                    }
                                    map[s] = array
                                }
                            }
                        }
                        data.loadQuest(map)
                        data.loadVariables(collection)
                    } catch (ex: Exception) {
                        QuestAdder.warn("unable to load the player data of ${player.uniqueId}.")
                    }
                    return data
                }

                override fun save(adder: QuestAdder, player: OfflinePlayer, playerData: PlayerData): Boolean {
                    val save = playerData.saveVariables()
                    val map = playerData.saveQuest()
                    return try {
                        YamlConfiguration().run {
                            set("variables",save.map {
                                "${it.first},${it.second},${it.third}"
                            }.toTypedArray())
                            set("quests",MemoryConfiguration().apply {
                                for (mutableEntry in map) {
                                    set(mutableEntry.key,MemoryConfiguration().apply {
                                        mutableEntry.value.forEach {
                                            set(it.first,it.second)
                                        }
                                    })
                                }
                            })
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