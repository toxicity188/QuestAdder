package kor.toxicity.questadder.util.database

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.data.QuestData
import kor.toxicity.questadder.extension.getAsStringList
import kor.toxicity.questadder.mechanic.quest.QuestRecord
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.sql.DriverManager
import java.time.LocalDateTime

enum class StandardDatabaseSupplier: DatabaseSupplier {
    YML {
        override fun supply(section: ConfigurationSection): Database {
            return object : Database {
                private fun getFile(adder: QuestAdder, player: OfflinePlayer) = File(File(adder.dataFolder.apply {
                    mkdir()
                },"users").apply {
                    mkdir()
                },"${player.uniqueId}.yml")

                override fun close() {
                }

                override fun load(adder: QuestAdder, player: OfflinePlayer): PlayerData {
                    val data = PlayerData()
                    try {
                        val collection = ArrayList<Triple<String, String, String>>()
                        val map = HashMap<String,QuestData>()
                        YamlConfiguration().run {
                            load(getFile(adder,player))
                            getAsStringList("variables")?.forEach {
                                val split = it.split(',')
                                if (split.size == 3) collection.add(Triple(split[0], split[1], split[2]))
                            }
                            getConfigurationSection("quests")?.let {
                                it.getKeys(false).forEach { s ->
                                    it.getConfigurationSection(s)?.let { config ->
                                        val time = config.getString("time") ?: return@forEach
                                        val state = config.getString("state") ?: return@forEach
                                        try {
                                            map[s] = QuestData(LocalDateTime.parse(time), QuestRecord.valueOf(state) , HashMap<String, Long>().apply {
                                                config.getConfigurationSection("variables")?.let { variable ->
                                                    variable.getKeys(false).forEach { key ->
                                                        put(key,variable.getLong(key))
                                                    }
                                                }
                                            })
                                        } catch (ex: Exception) {
                                            QuestAdder.warn("unable to load quest data of \"${player.name}\"")
                                        }
                                    }
                                }
                            }
                            getConfigurationSection("indexes")?.let {
                                it.getKeys(false).forEach { s ->
                                    data.npcIndexes[s] = it.getInt(s)
                                }
                            }
                        }
                        data.questVariables.putAll(map)
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
                            set("quests",MemoryConfiguration().apply {
                                for (questVariable in playerData.questVariables) {
                                    set(questVariable.key,MemoryConfiguration().apply {
                                        val v = questVariable.value
                                        set("time",v.time.toString())
                                        set("state",v.state.toString())
                                        set("variables", MemoryConfiguration().apply {
                                            v.variable.forEach { entry ->
                                                set(entry.key,entry.value)
                                            }
                                        })
                                    })
                                }
                            })
                            set("indexes",MemoryConfiguration().apply {
                                for (npcIndex in playerData.npcIndexes) {
                                    set(npcIndex.key,npcIndex.value)
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
            val host = section.getString("host") ?: throw RuntimeException("unable to find host.")
            val database = section.getString("database") ?: throw RuntimeException("unable to find database.")
            val name = section.getString("name") ?: throw RuntimeException("unable to find name.")
            val password = section.getString("password") ?: throw RuntimeException("unable to find password.")

            val mysql = DriverManager.getConnection("jdbc:mysql://$host/$database?autoReconnect=true&useSSL=false", name, password).apply {
                createStatement().use {
                    it.execute("CREATE TABLE IF NOT EXISTS indexes(uuid CHAR(36) NOT NULL, name VARCHAR(255) NOT NULL, value INT UNSIGNED NOT NULL, PRIMARY KEY(uuid,name));")
                    it.execute("CREATE TABLE IF NOT EXISTS variables(uuid CHAR(36) NOT NULL, name VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, value TEXT(65536) NOT NULL, PRIMARY KEY(uuid,name));")
                    it.execute("CREATE TABLE IF NOT EXISTS quests(uuid CHAR(36) NOT NULL, name VARCHAR(255) NOT NULL, value TEXT(65535) NOT NULL, PRIMARY KEY(uuid,name));")
                }
            }

            return object : Database {
                override fun close() {
                    mysql.close()
                }

                override fun save(adder: QuestAdder, player: OfflinePlayer, playerData: PlayerData): Boolean {
                    val uuid = player.uniqueId.toString()
                    mysql.run {
                        prepareStatement("DELETE FROM variables WHERE uuid = '$uuid';").use {
                            it.executeUpdate()
                        }
                        playerData.saveVariables().forEach { triple ->
                            prepareStatement("INSERT INTO variables(uuid,type,name,value) VALUES (?,?,?,?);").use {
                                it.setString(1, uuid)
                                it.setString(2,triple.first)
                                it.setString(3,triple.second)
                                it.setString(4,triple.third)
                                it.executeUpdate()
                            }
                        }
                        prepareStatement("DELETE FROM indexes WHERE uuid = '$uuid';").use {
                            it.executeUpdate()
                        }
                        playerData.npcIndexes.forEach { index ->
                            prepareStatement("INSERT INTO indexes(uuid, name, value) VALUES(?, ?, ?);").use {
                                it.setString(1,uuid)
                                it.setString(2,index.key)
                                it.setInt(3,index.value)
                                it.executeUpdate()
                            }
                        }
                        prepareStatement("DELETE FROM quests WHERE uuid = '$uuid';").use {
                            it.executeUpdate()
                        }
                        playerData.questVariables.forEach { quest ->
                            prepareStatement("INSERT INTO quests(uuid, name, value) VALUES(?, ?, ?);").use {
                                it.setString(1,uuid)
                                it.setString(2,quest.key)
                                it.setString(3,quest.value.serialize())
                                it.executeUpdate()
                            }
                        }
                    }
                    return true
                }

                override fun load(adder: QuestAdder, player: OfflinePlayer): PlayerData {
                    val data = PlayerData()
                    val uuid = player.uniqueId.toString()
                    mysql.run {
                        val varArray = ArrayList<Triple<String,String,String>>()
                        prepareStatement("SELECT type, name, value FROM variables WHERE uuid = '$uuid';").use {
                            val set = it.executeQuery()
                            while (set.next()) {
                                varArray.add(
                                    Triple(
                                    set.getString("name"),
                                    set.getString("type"),
                                    set.getString("value")
                                )
                                )
                            }
                        }
                        data.loadVariables(varArray)
                        prepareStatement("SELECT name, value FROM indexes WHERE uuid = '$uuid';").use {
                            val set = it.executeQuery()
                            while (set.next()) {
                                data.npcIndexes[set.getString("name")] = set.getInt("value")
                            }
                        }
                        prepareStatement("SELECT name, value FROM quests WHERE uuid = '$uuid';").use {
                            val set = it.executeQuery()
                            while (set.next()) {
                                QuestData.deserialize(set.getString("value"))?.let { questData ->
                                    data.questVariables[set.getString("name")] = questData
                                }
                            }
                        }
                    }
                    return data
                }
            }
        }
    }
}