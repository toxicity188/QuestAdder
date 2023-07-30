package kor.toxicity.questadder.util.function

interface ArgumentFunction: QuestFunction {
    fun apply(target: Any, args: Array<Any>): Any?
    fun getArgumentType(): List<Class<*>>
}