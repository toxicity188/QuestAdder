package kor.toxicity.questadder.util.function

import kor.toxicity.questadder.util.Nameable

interface QuestFunction: Nameable {
    fun getType(): Class<*>
    fun getReturnType(): Class<*>
}