package kor.toxicity.questadder.block

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Fire

data class FireBlockData(
    val age: Int,
    val west: Boolean,
    val up: Boolean,
    val south: Boolean,
    val north: Boolean,
    val east: Boolean
): QuestBlockData {
    companion object {
        fun fromHash(hash: Int): FireBlockData {
            val bit = hash and ((1 shl 10) - 1)

            return FireBlockData(
                (bit ushr 6),
                ((bit ushr 1) and 1) == 1,
                ((bit ushr 2) and 1) == 1,
                ((bit ushr 3) and 1) == 1,
                ((bit ushr 4) and 1) == 1,
                ((bit ushr 5) and 1) == 1,
            )
        }
        fun fromBlock(blockData: Fire): FireBlockData {
            val direction = blockData.faces
            return FireBlockData(
                blockData.age,
                direction.contains(BlockFace.WEST),
                direction.contains(BlockFace.UP),
                direction.contains(BlockFace.SOUTH),
                direction.contains(BlockFace.NORTH),
                direction.contains(BlockFace.EAST)
            )
        }
    }

    override fun createBlockData(): BlockData {
        return (Material.FIRE.createBlockData() as Fire).also {
            it.age = age
            it.setFace(BlockFace.WEST, west)
            it.setFace(BlockFace.UP, up)
            it.setFace(BlockFace.SOUTH, south)
            it.setFace(BlockFace.NORTH, north)
            it.setFace(BlockFace.EAST, east)
        }
    }

    override fun toKey(): String = "age=$age,west=$west,up=$up,south=$south,north=$north,east=$east"
}