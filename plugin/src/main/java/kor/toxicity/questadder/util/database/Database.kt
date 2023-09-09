package kor.toxicity.questadder.util.database

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.data.PlayerData
import org.bukkit.OfflinePlayer

interface Database {
    fun load(adder: QuestAdderBukkit, player: OfflinePlayer): PlayerData
    fun save(adder: QuestAdderBukkit, player: OfflinePlayer, playerData: PlayerData): Boolean
    fun close()
}