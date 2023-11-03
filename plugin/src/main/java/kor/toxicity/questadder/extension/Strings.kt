package kor.toxicity.questadder.extension

import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.Null
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.round

val EMPTY_DECORATION = mapOf(
    TextDecoration.ITALIC to TextDecoration.State.FALSE,
    TextDecoration.BOLD to TextDecoration.State.FALSE,
    TextDecoration.UNDERLINED to TextDecoration.State.FALSE,
    TextDecoration.STRIKETHROUGH to TextDecoration.State.FALSE,
    TextDecoration.OBFUSCATED to TextDecoration.State.FALSE
)

val ANNOTATION_PATTERN: Pattern = Pattern.compile("@((?<name>(\\w|_|-)+):(?<value>(\\w|[가-힣]|_|,|-)+))")
fun String.colored(): Component = ComponentReader<Null>(this).createComponent(Null) ?: asComponent()
fun List<String>.colored() = map {
    it.colored()
}
fun Component.clear() = deepDecorations(EMPTY_DECORATION)
fun Component.deepClear() = clear().deepColor(WHITE)
fun Number.withComma(): String = DecimalFormat.getInstance().format(round(toDouble()).toInt())

fun Int.parseChar(): String {
    return if (this <= 0xFFFF) toChar().toString()
    else {
        val t = this - 0x10000
        "${((t ushr 10) + 0xD800).toChar()}${((t and 1023) + 0xDC00).toChar()}"
    }
}
