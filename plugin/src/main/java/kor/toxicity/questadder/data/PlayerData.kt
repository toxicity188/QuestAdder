package kor.toxicity.questadder.data

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.mechanic.IQuestData
import kor.toxicity.questadder.api.mechanic.QuestRecord
import kor.toxicity.questadder.api.util.IPlayerData
import kor.toxicity.questadder.extension.getAsStringList
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.variable.SerializeManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class PlayerData: IPlayerData {
    companion object {
        fun deserialize(section: ConfigurationSection): PlayerData {
            val data = PlayerData()
            val collection = ArrayList<Triple<String, String, String>>()
            val map = HashMap<String,QuestData>()
            section.run {
                getAsStringList("variables")?.forEach {
                    val split = it.split(',')
                    if (split.size == 3) collection.add(Triple(split[0], split[1], split[2]))
                }
                getConfigurationSection("quests")?.let {
                    it.getKeys(false).forEach { s ->
                        it.getConfigurationSection(s)?.let { config ->
                            val time = config.getString("time") ?: return@forEach
                            val state = config.getString("state") ?: return@forEach
                            map[s] = QuestData(LocalDateTime.parse(time), QuestRecord.valueOf(state) , HashMap<String, Long>().apply {
                                config.getConfigurationSection("variables")?.let { variable ->
                                    variable.getKeys(false).forEach { key ->
                                        put(key,variable.getLong(key))
                                    }
                                }
                            })
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
            return data
        }
    }
    override fun serialize(): ConfigurationSection {
        val save = saveVariables()
        return YamlConfiguration().apply {
            set("variables",save.map {
                "${it.first},${it.second},${it.third}"
            }.toTypedArray())
            set("quests", MemoryConfiguration().apply {
                for (questVariable in questVariables) {
                    set(questVariable.key, MemoryConfiguration().apply {
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
            set("indexes", MemoryConfiguration().apply {
                for (npcIndex in npcIndexes) {
                    set(npcIndex.key,npcIndex.value)
                }
            })
        }
    }



    private val variables = HashMap<String,Any>()
    val questVariables: MutableMap<String,QuestData> = ConcurrentHashMap()
    val npcIndexes = HashMap<String,Int>()


    fun saveVariables() = ArrayList<Triple<String,String,String>>().apply {
        variables.forEach {
            if (!it.key.startsWith('_')) SerializeManager.trySerialize(it.value)?.let { str ->
                add(Triple(it.key,it.value.javaClass.name,str))
            }
        }
    }
    fun loadVariables(collection: Collection<Triple<String, String, String>>) {
        collection.forEach {
            try {
                SerializeManager.tryDeserialize(Class.forName(it.second),it.third)?.let { value ->
                    variables[it.first] = value
                }
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("unable to load player data named \"${it.first}\"")
            }
        }
    }

    override fun getQuestDataMap(): MutableMap<String, out IQuestData> {
        return questVariables
    }

    fun get(name: String) = variables[name] ?: Null
    fun set(name: String, any: Any) {
        variables[name] = any
    }
    fun putIfAbsent(name: String, any: Any) {
        variables.putIfAbsent(name,any)
    }
    fun remove(name: String) = variables.remove(name) != null

    fun getQuestVariable(quest: String, name: String) = questVariables[quest]?.variable?.get(name)
    fun setQuestVariable(quest: String, name: String, long: Long) = questVariables[quest]?.variable?.put(name,long)
    fun giveQuest(quest: String) {
        questVariables[quest] = QuestData(LocalDateTime.now(),QuestRecord.HAS,HashMap())
    }
    fun hasQuest(quest: String) = questVariables[quest]?.state == QuestRecord.HAS
    fun removeQuest(quest: String) = questVariables.remove(quest)
    fun completeQuest(quest: String) {
        questVariables[quest]?.let {
            it.state = QuestRecord.COMPLETE
            it.time = LocalDateTime.now()
        }
    }

    fun getQuestKey() = questVariables.keys


}
