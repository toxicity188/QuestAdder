package kor.toxicity.questadder.util.gui

import kor.toxicity.questadder.QuestAdder


abstract class SubExecutor(private val beforeData: GuiData): GuiExecutor {

    var safeEnd = false

    override fun end(data: GuiData) {
        if (!safeEnd) {
            QuestAdder.taskLater(1) {
                beforeData.reopen()
            }
        }
    }
}