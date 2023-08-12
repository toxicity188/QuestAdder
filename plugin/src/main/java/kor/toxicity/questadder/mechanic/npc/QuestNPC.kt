package kor.toxicity.questadder.mechanic.npc

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findInt
import kor.toxicity.questadder.extension.findString
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.util.SoundData
import kor.toxicity.questadder.util.gui.Gui
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.configuration.ConfigurationSection
import java.io.File

class QuestNPC(adder: QuestAdder, file: File, val key: String, section: ConfigurationSection) {
    val id = section.findInt(0,"id","Id")
    val name = section.getString("name") ?: key
    val soundData = section.findConfig("typing-sound","TypingSound")?.let { t ->
        SoundData.fromConfig(t)
    }
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

    fun getNpc(): NPC? = CitizensAPI.getNPCRegistry().getById(id)
}