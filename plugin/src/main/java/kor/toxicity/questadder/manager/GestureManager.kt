package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.send
import org.bukkit.Bukkit
import java.io.File

object GestureManager: QuestAdderManager {
    override fun start(adder: QuestAdder) {

    }

    override fun reload(adder: QuestAdder) {
        File(adder.dataFolder.apply {
            mkdir()
        },"gestures").run {
            mkdir()
            listFiles()?.forEach {
                if (it.extension == "bbmodel") {
                    QuestAdder.animator.animationManager.importAnimations("questadder",it)
                }
            }
        }
        Bukkit.getConsoleSender().send("${QuestAdder.animator.animationManager.registry.size} of gestures has successfully loaded.")
    }

    override fun end(adder: QuestAdder) {
    }
}