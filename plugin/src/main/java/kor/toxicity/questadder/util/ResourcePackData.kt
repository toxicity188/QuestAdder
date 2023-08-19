package kor.toxicity.questadder.util

import org.bukkit.Material

class ResourcePackData(
    val material: Material,
    val assets: String,
    val action: (Int) -> Unit
)