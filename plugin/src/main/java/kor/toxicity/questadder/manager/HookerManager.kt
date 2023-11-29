package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.GiveRewardEvent
import kor.toxicity.questadder.api.exp.ExpHandler
import kor.toxicity.questadder.extension.getAsStringList
import kor.toxicity.questadder.hooker.exp.ExpHandlerType
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object HookerManager: QuestAdderManager {
    private val expHandler = ArrayList<ExpHandler>()
    override fun start(adder: QuestAdderBukkit) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            fun reward(e: GiveRewardEvent) {
                val exp = e.exp
                val player = e.player
                val iterator = expHandler.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    try {
                        next.accept(player, exp)
                    } catch (ex: Throwable) {
                        QuestAdderBukkit.warn("an error has occurred while applying ${next.requiredPlugin()}'s exp.")
                        iterator.remove()
                    }
                }
            }
        },adder)
    }

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
        checker(0.0, "loading exp hooker...")
        expHandler.clear()
        adder.loadFile("exp")?.let {
            it.getAsStringList("exp-hooker")?.forEach { str ->
                try {
                    expHandler.add(ExpHandlerType.valueOf(str.uppercase()).supply())
                } catch (ex: Throwable) {
                    QuestAdderBukkit.warn("unable to load exp hooker \"$str\"")
                }
            }
        }
        checker(1.0, "finalizing exp hooker...")
    }

    override fun end(adder: QuestAdderBukkit) {
    }
}
