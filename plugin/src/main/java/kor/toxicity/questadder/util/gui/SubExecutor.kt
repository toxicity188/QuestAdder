package kor.toxicity.questadder.util.gui

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.GuiExecutor


abstract class SubExecutor(private val beforeData: GuiData): GuiExecutor {

    var safeEnd = false

    override fun end(data: GuiData) {
        if (!safeEnd) {
            QuestAdderBukkit.taskLater(1) {
                beforeData.reopen()
            }
        }
    }
}