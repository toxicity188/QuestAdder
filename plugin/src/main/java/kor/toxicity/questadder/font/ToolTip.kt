package kor.toxicity.questadder.font

data class ToolTip(
    val gui: GuiFontData,
    val fade: Boolean,
    val split: Int,
    val offset: Int,
    val chatOffset: Int,
    val talker: ToolTipFontData,
    val talk: List<ToolTipFontData>
)
