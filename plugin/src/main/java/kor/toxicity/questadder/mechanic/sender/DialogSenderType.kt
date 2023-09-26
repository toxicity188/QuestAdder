package kor.toxicity.questadder.mechanic.sender

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.mechanic.DialogSender
import org.bukkit.configuration.ConfigurationSection

enum class DialogSenderType {
    ITEM {
        override fun create(adder: QuestAdderBukkit, key: String, section: ConfigurationSection): DialogSender {
            return ItemDialogSender(adder, key, section)
        }
    }
    ;
    abstract fun create(adder: QuestAdderBukkit, key: String, section: ConfigurationSection): DialogSender
}