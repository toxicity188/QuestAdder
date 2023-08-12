package kor.toxicity.questadder.nms

import net.kyori.adventure.text.Component
import org.bukkit.Location

interface VirtualArmorStand {
    fun remove()
    fun teleport(location: Location)
    fun setText(text: Component)
}