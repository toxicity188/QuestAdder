package kor.toxicity.questadder.mechanic.npc

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.mechanic.IQuestNPC
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.mechanic.quest.Quest
import kor.toxicity.questadder.util.gui.Gui
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File
import java.util.function.Consumer

class QuestNPC(adder: QuestAdderBukkit, file: File, val npcKey: String, section: ConfigurationSection): IQuestNPC {
    val id = section.findInt(0,"id","Id")
    val name = section.getString("name") ?: npcKey
    val soundData = section.findSoundData("typing-sound","TypingSound") ?: QuestAdderBukkit.Config.defaultTypingSound
    val dialogs: List<Dialog> = ArrayList<Dialog>().apply {
        adder.addLazyTask {
            section.findStringList("dialogs","Dialog","dialog","Dialogs")?.mapNotNull {
                DialogManager.getDialog(it).apply {
                    if (this == null) QuestAdderBukkit.warn("not found error: unable to found the dialog named \"$it\". ($npcKey in ${file.name})")
                    else add(this)
                }
            }?.apply {
                if (isEmpty()) QuestAdderBukkit.warn("npc has empty dialog. ($npcKey in ${file.name})")
            } ?: QuestAdderBukkit.warn("npc has no dialog. ($npcKey in ${file.name})")
        }
    }
    val inventory = section.findConfig("Inventory","inventory")?.let {
        try {
            Gui(5,it)
        } catch (ex: Exception) {
            QuestAdderBukkit.warn("unable to load NPC's inventory ($npcKey in ${file.name})")
            QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
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
                            } ?: QuestAdderBukkit.warn("not found error: the quest named \"$t\" doesn't exist. ($npcKey in ${file.name})")
                        }
                    } catch (ex: Exception) {
                        QuestAdderBukkit.warn("number format error: the key \"$s\" is not integer. ($npcKey in ${file.name})")
                    }
                } ?: QuestAdderBukkit.warn("syntax error: the value of \"$s\" is not string. ($npcKey in ${file.name})")
            }
        }
    }
    val thread = section.findLong(20,"Thread","thread").coerceAtLeast(1)
    val renderDistance = section.findDouble(16.0,"render-distance","RenderDistance").coerceAtLeast(3.0)

    val typingSpeed = section.findLong(QuestAdderBukkit.Config.defaultTypingSpeed,"typing-speed","TypingSpeed")

    override fun getIndex(player: Player) = QuestAdderBukkit.getPlayerData(player)?.npcIndexes?.get(npcKey)
    override fun setIndex(player: Player, index: Int) = QuestAdderBukkit.getPlayerData(player)?.npcIndexes?.let {
        it[npcKey] = index
        true
    } ?: false

    override fun toString(): String {
        return name
    }

    override fun getKey(): String {
        return npcKey
    }

    override fun getIndexAsync(player: Player, consumer: Consumer<Int>) {
        QuestAdderBukkit.getPlayerData(player) {
            consumer.accept(it.npcIndexes[npcKey] ?: -1)
        }
    }

    override fun setIndexAsync(player: OfflinePlayer, index: Int) {
        QuestAdderBukkit.getPlayerData(player) {
            it.npcIndexes[npcKey] = index
        }
    }
}