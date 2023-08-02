package kor.toxicity.questadder.data

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.variable.SerializeManager

class PlayerData {
    private val variables = HashMap<String,Any>()
    private val questVariables = HashMap<String,MutableMap<String,Long>>()


    fun saveVariables() = ArrayList<Triple<String,String,String>>().apply {
        variables.forEach {
            SerializeManager.trySerialize(it.value)?.let { str ->
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
                QuestAdder.warn("unable to load player data named \"${it.first}\"")
            }
        }
    }

    fun get(name: String) = variables[name] ?: Null
    fun set(name: String, any: Any) {
        variables[name] = any
    }
    fun putIfAbsent(name: String, any: Any) {
        variables.putIfAbsent(name,any)
    }
    fun remove(name: String) = variables.remove(name) != null

    fun getQuestVariable(quest: String, name: String) = questVariables[quest]?.get(name)
    fun setQuestVariable(quest: String, name: String, long: Long) = questVariables[quest]?.put(name,long)
    fun giveQuest(quest: String) {
        questVariables[quest] = HashMap()
    }
    fun removeQuest(quest: String) = questVariables.remove(quest)

    fun getQuestKey() = questVariables.keys
}