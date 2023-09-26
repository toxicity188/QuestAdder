package kor.toxicity.questadder.block

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing

data class RedMushroomBlockData(
    val west: Boolean,
    val up: Boolean,
    val south: Boolean,
    val north: Boolean,
    val east: Boolean,
    val down: Boolean
): QuestBlockData {
    companion object {
        fun fromHash(hash: Int): RedMushroomBlockData {
            val bit = hash and ((1 shl 6) - 1)

            return RedMushroomBlockData(
                (bit and 1) == 1,
                ((bit ushr 1) and 1) == 1,
                ((bit ushr 2) and 1) == 1,
                ((bit ushr 3) and 1) == 1,
                ((bit ushr 4) and 1) == 1,
                ((bit ushr 5) and 1) == 1,
            )
        }
        fun fromBlock(blockData: MultipleFacing): RedMushroomBlockData {
            val direction = blockData.faces
            return RedMushroomBlockData(
                direction.contains(BlockFace.WEST),
                direction.contains(BlockFace.UP),
                direction.contains(BlockFace.SOUTH),
                direction.contains(BlockFace.NORTH),
                direction.contains(BlockFace.EAST),
                direction.contains(BlockFace.DOWN)
            )
        }
    }

    override fun toKey(): String = "west=$west,up=$up,south=$south,north=$north,east=$east,down=$down"

    override fun createBlockData(): BlockData = (Material.RED_MUSHROOM_BLOCK.createBlockData() as MultipleFacing).also {
        it.setFace(BlockFace.WEST, west)
        it.setFace(BlockFace.UP, up)
        it.setFace(BlockFace.SOUTH, south)
        it.setFace(BlockFace.NORTH, north)
        it.setFace(BlockFace.EAST, east)
        it.setFace(BlockFace.DOWN, down)
    }
}