package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder

interface QuestAdderManager {
    fun start(adder: QuestAdder)
    fun reload(adder: QuestAdder)
    fun end(adder: QuestAdder)
}