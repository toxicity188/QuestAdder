package kor.toxicity.questadder.util.variable

interface VariableSerializer {
    fun serialize(any: Any): String
    fun deserialize(string: String): Any?
}