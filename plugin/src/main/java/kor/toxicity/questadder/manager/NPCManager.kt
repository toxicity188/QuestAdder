package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.mechanic.npc.QuestNPC
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.CitizensReloadEvent
import net.citizensnpcs.api.event.NPCDespawnEvent
import net.citizensnpcs.api.event.NPCSpawnEvent
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import java.util.UUID

object NPCManager: QuestAdderManager {

    private val questNpcMap = HashMap<String, QuestNPC>()
    private val actualNPCMap = HashMap<UUID, ActualNPC>()

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun click(e: PlayerInteractAtEntityEvent) {
                val uuid = e.rightClicked.uniqueId
                actualNPCMap[uuid]?.let {
                    val questNpc = it.questNPC
                    questNpc.dialogVar?.let { s ->
                        QuestAdder.getPlayerData(e.player)?.get(s)?.let { v ->
                            if (v is Number) questNpc.dialogs[v.toInt().coerceAtLeast(0).coerceAtMost(questNpc.dialogs.lastIndex)].start(e.player,it)
                        } ?: QuestAdder.warn("runtime error: the variable \"$s\" is not an int!")
                    } ?: questNpc.dialogs.first().start(e.player,it)
                }
            }
            @EventHandler
            fun spawn(e: NPCSpawnEvent) {
                registerNPC(e.npc)
            }
            @EventHandler
            fun deSpawn(e: NPCDespawnEvent) {
                actualNPCMap.remove(e.npc.entity.uniqueId)
            }
            @EventHandler
            fun reload(e: CitizensReloadEvent) {
                reloadCitizen(adder)
            }
        },adder)
    }

    private fun registerNPC(npc: NPC) {
        questNpcMap.values.firstOrNull {
            npc.id == it.id
        }?.let {
            actualNPCMap[npc.entity.uniqueId] = ActualNPC(npc,it)
        }
    }
    override fun reload(adder: QuestAdder) {
        reloadNPC(adder)
    }
    private fun reloadNPC(adder: QuestAdder) {
        reloadCitizen(adder)
    }
    private fun reloadCitizen(adder: QuestAdder) {

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
        CitizensAPI.getNPCRegistry().forEach {
            registerNPC(it)
        }
        Bukkit.getConsoleSender().send("${questNpcMap.size} of NPCs successfully loaded.")
    }
    override fun end(adder: QuestAdder) {
    }
}