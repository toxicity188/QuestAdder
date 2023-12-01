package kor.toxicity.questadder.mechanic.sender

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.gui.IGui
import kor.toxicity.questadder.api.mechanic.IDialogState
import kor.toxicity.questadder.api.mechanic.IItemDialogSender
import kor.toxicity.questadder.api.util.SoundData
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.mechanic.dialog.Dialog
import kor.toxicity.questadder.util.gui.Gui
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.function.Supplier

class ItemDialogSender(adder: QuestAdderBukkit, key: String, section: ConfigurationSection): IItemDialogSender {
    val item = section.findItemStack("Item","item") {
        it.persistentDataContainer.set(QUEST_ADDER_SENDER_KEY, PersistentDataType.STRING, key)
    } ?: throw RuntimeException("item value not found.")
    val consume = section.findBoolean("Consume","consume")
    var dialog: Dialog? = null
    private val name = section.findString("Name", "name") ?: throw RuntimeException("name value not found.")
    private val sound = section.findSoundData("Sound", "sound") ?: QuestAdderBukkit.Config.defaultTypingSound
    private val speed = section.findLong(QuestAdderBukkit.Config.defaultTypingSpeed, "typing-speed", "TypingSpeed")
    private val inventory = section.findConfig("Inventory","inventory")?.let {
        Gui(5,it)
    }
    init {
        val d = section.findString("Dialog", "dialog") ?: throw RuntimeException("dialog value not found.")
        adder.addLazyTask {
            dialog = DialogManager.getDialog(d)
            if (dialog == null) QuestAdderBukkit.warn("the dialog named \"$d\" doesn't exist.")
        }
    }

    override fun getLocationSupplier(): Supplier<Location>? {
        return null
    }

    override fun getSoundData(): SoundData {
        return sound
    }

    override fun getTalkerName(): String {
        return name
    }

    override fun getTypingSpeed(): Long {
        return speed
    }

    override fun getGui(): IGui? {
        return inventory
    }

    override fun give(player: Player) {
        player.give(item)
    }

    override fun start(player: Player): IDialogState? {
        return dialog?.start(player, this)
    }
}
