package kor.toxicity.questadder.nms

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

interface NMSChannel {
    fun inject(player: Player)
    fun uninject(player: Player)
    fun openSign(player: Player, array: List<Component>, callback: (Array<String>) -> Unit)
}
