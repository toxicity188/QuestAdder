package kor.toxicity.questadder.nms

import org.bukkit.Location

interface VirtualEntity {
    fun remove()
    fun teleport(location: Location)
}