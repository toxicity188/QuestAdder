package kor.toxicity.questadder.manager.registry

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.block.IQuestBlock
import kor.toxicity.questadder.api.registry.IBlockRegistry
import kor.toxicity.questadder.block.NoteBlockData
import kor.toxicity.questadder.block.QuestBlock
import kor.toxicity.questadder.block.QuestBlockData
import kor.toxicity.questadder.block.StringBlockData
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.block.data.type.Tripwire
import org.bukkit.configuration.ConfigurationSection

class BlockRegistry: IBlockRegistry {
    private val stringMap = HashMap<String, QuestBlock>()
    private val dataMap = HashMap<QuestBlockData, QuestBlock>()

    override fun findByBlock(data: BlockData): IQuestBlock? = get(data)

    override fun findByKey(key: String): IQuestBlock? = get(key)
    fun get(key: String) = stringMap[key]
    fun get(data: BlockData): QuestBlock? {
        return when (data) {
            is NoteBlock -> dataMap[NoteBlockData.fromBlock(data)]
            is Tripwire -> dataMap[StringBlockData.fromBlock(data)]
            else -> null
        }
    }
    override fun getAllKeys(): Set<String> {
        return stringMap.keys
    }

    fun tryRegister(key: String, section: ConfigurationSection): QuestBlockData? {
        if (stringMap.containsKey(key)) {
            QuestAdderBukkit.warn("the name \"$key\" always exist. you should change it's name.")
            return null
        }
        val block = QuestBlock(key, section)
        if (dataMap.containsKey(block.data)) {
            QuestAdderBukkit.warn("hash collision detected: \"$key\"")
            QuestAdderBukkit.warn("you should change it's name.")
            return null
        }
        stringMap[key] = block
        dataMap[block.data] = block
        return block.data
    }

    fun clear() {
        stringMap.clear()
        dataMap.clear()
    }

    override fun getAllBlock(): Collection<IQuestBlock> {
        return stringMap.values
    }
}