package kor.toxicity.questadder.block

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Tripwire

data class StringBlockData(
    val attached: Boolean,
    val disarmed: Boolean,
    val east: Boolean,
    val north: Boolean,
    val powered: Boolean,
    val south: Boolean,
    val west: Boolean
): QuestBlockData {
    companion object {
        fun fromHash(hash: Int): StringBlockData {
            val bit = hash and ((1 shl 7) - 1)

            return StringBlockData(
                (bit and 1) == 1,
                ((bit ushr 1) and 1) == 1,
                ((bit ushr 2) and 1) == 1,
                ((bit ushr 3) and 1) == 1,
                ((bit ushr 4) and 1) == 1,
                ((bit ushr 5) and 1) == 1,
                ((bit ushr 6) and 1) == 1,
            )
        }
        fun fromBlock(tripwire: Tripwire) = StringBlockData(
            tripwire.isAttached,
            tripwire.isDisarmed,
            tripwire.faces.contains(BlockFace.EAST),
            tripwire.faces.contains(BlockFace.NORTH),
            tripwire.isPowered,
            tripwire.faces.contains(BlockFace.SOUTH),
            tripwire.faces.contains(BlockFace.WEST)
        )
    }

    override fun toKey(): String {
        return "attached=$attached,disarmed=$disarmed,east=$east,north=$north,powered=$powered,south=$south,west=$west"
    }

    override fun createBlockData(): BlockData {
        return (Material.TRIPWIRE.createBlockData() as Tripwire).also {
            it.isAttached = attached
            it.isDisarmed = disarmed
            it.isPowered = powered

            it.setFace(BlockFace.NORTH, north)
            it.setFace(BlockFace.EAST, east)
            it.setFace(BlockFace.SOUTH, south)
            it.setFace(BlockFace.WEST, west)
        }
    }
}