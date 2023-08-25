package kor.toxicity.questadder.util

import kor.toxicity.questadder.util.builder.FunctionBuilder

class TextReader<T: Any>(string: String) {
    private val text = ArrayList<(T) -> String>()

    init {
        val split = ComponentReader.splitFunction(string)
        split.forEachIndexed { index, s ->
            if (index % 2 == 0) {
                text.add {
                    s
                }
            } else {
                val function = FunctionBuilder.evaluate(s)
                text.add {
                    function.apply(it)?.toString() ?: "<none>"
                }
            }
        }
    }

    fun createString(t: T) = try {
        val builder = StringBuilder()
        text.forEach {
            builder.append(it(t))
        }
        builder.toString()
    } catch (ex: Exception) {
        null
    }
}