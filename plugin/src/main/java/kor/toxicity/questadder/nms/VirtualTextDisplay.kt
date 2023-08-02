package kor.toxicity.questadder.nms

import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.UUID

interface VirtualTextDisplay {
    fun getUUID(): UUID
    fun teleport(location: Location)
    fun remove()
    fun setTransform(x: Float, y: Float, z: Float, pitch: Double, yaw: Double, roll: Double, scale: Float)
    fun setText(text: Component)
    fun setOpacity(byte: Byte)
}