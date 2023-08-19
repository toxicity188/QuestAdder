package kor.toxicity.questadder.mechanic.npc

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.mechanic.quest.Quest
import kor.toxicity.questadder.util.SoundData
import kor.toxicity.questadder.util.gui.Gui
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File

class QuestNPC(adder: QuestAdder, file: File, val key: String, section: ConfigurationSection) {
    val id = section.findInt(0,"id","Id")
    val name = section.getString("name") ?: key
    val soundData = section.findConfig("typing-sound","TypingSound")?.let { t ->
        SoundData.fromConfig(t)
    } ?: QuestAdder.Config.defaultTypingSound
    val dialogs: List<Dialog> = ArrayList<Dialog>().apply {
        adder.addLazyTask {
            section.findStringList("dialogs","Dialog","dialog","Dialogs")?.mapNotNull {
                DialogManager.getDialog(it).apply {
                    if (this == null) QuestAdder.warn("not found error: unable to found the dialog named \"$it\". ($key in ${file.name})")
                    else add(this)
                }
            }?.apply {
                if (isEmpty()) QuestAdder.warn("npc has empty dialog. ($key in ${file.name})")
            } ?: QuestAdder.warn("npc has no dialog. ($key in ${file.name})")
        }
    }
    val inventory = section.findConfig("Inventory","inventory")?.let {
        try {
            Gui(5,it)
        } catch (ex: Exception) {
            QuestAdder.warn("unable to load NPC's inventory ($key in ${file.name})")
            QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            null
        }
    }
    val indicate: Map<Int, Quest> = HashMap<Int, Quest>().apply {
        section.findConfig("Indicate","indicate")?.let {
            it.getKeys(false).forEach { s ->
                it.getString(s)?.let { t ->
                    try {
                        val i = s.toInt()
                        adder.addLazyTask {
                            DialogManager.getQuest(t)?.let { quest ->
                                put(i,quest)
                                Unit
                            } ?: QuestAdder.warn("not found error: the quest named \"$t\" doesn't exist. ($key in ${file.name})")
                        }
                    } catch (ex: Exception) {
                        QuestAdder.warn("number format error: the key \"$s\" is not integer. ($key in ${file.name})")
                    }
                } ?: QuestAdder.warn("syntax error: the value of \"$s\" is not string. ($key in ${file.name})")
            }
        }
    }
    val thread = section.findLong(20,"Thread","thread").coerceAtLeast(1)
    val renderDistance = section.findDouble(16.0,"render-distance","RenderDistance").coerceAtLeast(3.0)

    fun getNpc(): NPC? = CitizensAPI.getNPCRegistry().getById(id)

    fun getIndex(player: Player) = QuestAdder.getPlayerData(player)?.npcIndexes?.get(key)

    override fun toString(): String {
        return name
    }
}