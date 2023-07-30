package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findInt
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.util.SoundData
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import org.bukkit.configuration.ConfigurationSection
import java.io.File

class QuestNPC(file: File, key: String, section: ConfigurationSection) {
    val id = section.findInt(0,"id","Id")
    val name = section.getString("name") ?: key
    val soundData = section.findConfig("typing-sound","TypingSound")?.let { t ->
        SoundData.fromConfig(t)
    }
    val dialogs = section.findStringList("dialogs","Dialog","dialog","Dialogs")?.mapNotNull {
        DialogManager.getDialog(it).apply {
            if (this == null) QuestAdder.warn("not found error: unable to found the dialog named \"$it\". ($key in ${file.name})")
        }
    }?.apply {
        if (isEmpty()) throw RuntimeException("npc has empty dialog.")
    } ?: throw RuntimeException("npc has no dialog.")

    fun getNpc(): NPC? = CitizensAPI.getNPCRegistry().getById(id)
}