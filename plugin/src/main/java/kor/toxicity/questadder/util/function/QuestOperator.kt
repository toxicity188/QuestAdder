package kor.toxicity.questadder.util.function

import kor.toxicity.questadder.util.Nameable

interface QuestOperator: Nameable {
    fun operate(a: Any?, b: Any?): Any
    fun getReturnType(): Class<*>
}