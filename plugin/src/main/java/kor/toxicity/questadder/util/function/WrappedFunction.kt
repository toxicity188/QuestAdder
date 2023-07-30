package kor.toxicity.questadder.util.function

interface WrappedFunction: QuestFunction {
    fun apply(t: Any): Any?
}