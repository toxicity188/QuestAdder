package kor.toxicity.questadder.hooker.exp

import kor.toxicity.questadder.api.exp.ExpHandler
import org.bukkit.entity.Player

class DefaultExpHandler: ExpHandler {
    override fun accept(player: Player, exp: Double) {
        player.exp += exp.toFloat()
    }

    override fun requiredPlugin(): String {
        return "QuestAdder"
    }
}