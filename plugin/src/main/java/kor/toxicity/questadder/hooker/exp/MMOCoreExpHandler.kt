package kor.toxicity.questadder.hooker.exp

import kor.toxicity.questadder.api.exp.ExpHandler
import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.MMOCoreAPI
import org.bukkit.entity.Player

class MMOCoreExpHandler: ExpHandler {
    private val api = MMOCoreAPI(MMOCore.plugin)
    override fun accept(player: Player, exp: Double) {
        api.getPlayerData(player).experience += exp
    }

    override fun requiredPlugin(): String {
        return "MMOCore"
    }
}