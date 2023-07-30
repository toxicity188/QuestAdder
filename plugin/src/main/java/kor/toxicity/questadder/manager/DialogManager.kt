package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.event.PlayerParseEvent
import kor.toxicity.questadder.extension.info
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.util.ComponentReader
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object DialogManager: QuestAdderManager {

    private val dialogMap = HashMap<String,Dialog>()

    override fun start(adder: QuestAdder) {

    }

    override fun reload(adder: QuestAdder) {
        dialogMap.clear()
        adder.command.addCommand("parse") {
            aliases = arrayOf("p")
            description = "parse result from given arguments."
            length = 1
            usage = "parse <text>"
            allowedSender = arrayOf(SenderType.PLAYER)
            executor = { sender, args ->
                val str = args.toMutableList().apply {
                    removeAt(0)
                }.joinToString(" ")
                ComponentReader<PlayerParseEvent>(str).createComponent(PlayerParseEvent(sender as Player).apply {
                    callEvent()
                })?.let { component ->
                    sender.info(component)
                } ?: sender.info("cannot parse this text argument.")
            }
        }
        adder.loadFolder("dialogs") { file, config ->
            config.getKeys(false).forEach {
                config.getConfigurationSection(it)?.let { c ->
                    dialogMap[it] = Dialog(adder,file,it,c)
                } ?: QuestAdder.warn("syntax error: the key '$it' is not a configuration section. (${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${dialogMap.size} of dialogs has successfully loaded.")
    }

    fun getDialog(name: String) = dialogMap[name]

    override fun end(adder: QuestAdder) {
    }
}