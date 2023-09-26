package kor.toxicity.questadder.mechanic.dialog

import kor.toxicity.questadder.api.mechanic.IDialogState

class DialogState: IDialogState {
    val tasks = ArrayList<() -> Unit>()
    override fun addEndTask(runnable: Runnable) {
        tasks.add {
            runnable.run()
        }
    }
}