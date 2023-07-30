package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.mechanic.QuestNPC
import net.citizensnpcs.api.event.CitizensReloadEvent
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import java.util.UUID

object NPCManager: QuestAdderManager {

    private val questNpcMap = HashMap<String,QuestNPC>()
    private val actualNPCMap = HashMap<UUID,QuestNPC>()

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun click(e: PlayerInteractAtEntityEvent) {
                val uuid = e.rightClicked.uniqueId
                actualNPCMap[uuid]?.let {
                    it.dialogs.first().start(e.player,it)
                }
            }
            @EventHandler
            fun citizensReload(e: CitizensReloadEvent) {
                reloadNPC(adder)
            }
        },adder)
    }

    override fun reload(adder: QuestAdder) {
        reloadNPC(adder)
    }
    private fun reloadNPC(adder: QuestAdder) {
        questNpcMap.clear()
        actualNPCMap.clear()
        adder.loadFolder("npcs") { file, c ->
            c.getKeys(false).forEach {
                c.getConfigurationSection(it)?.let { config ->
                    try {
                        questNpcMap[it] = QuestNPC(file, it, config)
                    } catch (ex: Exception) {
                        QuestAdder.warn("unable to load NPC. ($it in ${file.name})")
                        QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    }
                }
            }
        }
        QuestAdder.taskLater(20) {
            questNpcMap.forEach {
                it.value.getNpc()?.let { npc ->
                    actualNPCMap[npc.entity.uniqueId] = it.value
                }
            }
        }
        Bukkit.getConsoleSender().send("${questNpcMap.size} of NPCs successfully loaded.")
    }
    override fun end(adder: QuestAdder) {
    }
}