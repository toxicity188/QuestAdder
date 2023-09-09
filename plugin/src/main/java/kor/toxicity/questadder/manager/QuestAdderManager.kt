package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit

interface QuestAdderManager {
    fun start(adder: QuestAdderBukkit)
    fun reload(adder: QuestAdderBukkit)
    fun end(adder: QuestAdderBukkit)
}