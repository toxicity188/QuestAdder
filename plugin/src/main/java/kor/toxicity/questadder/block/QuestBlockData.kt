package kor.toxicity.questadder.block

import org.bukkit.block.data.BlockData

interface QuestBlockData {
    fun toKey(): String
    fun createBlockData(): BlockData
}