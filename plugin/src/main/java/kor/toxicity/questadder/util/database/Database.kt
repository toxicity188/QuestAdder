package kor.toxicity.questadder.util.database

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.data.PlayerData
import org.bukkit.OfflinePlayer

interface Database {
    fun load(adder: QuestAdder, player: OfflinePlayer): PlayerData
    fun save(adder: QuestAdder, player: OfflinePlayer, playerData: PlayerData): Boolean
    fun close()
}