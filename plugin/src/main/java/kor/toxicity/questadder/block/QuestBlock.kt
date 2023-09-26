package kor.toxicity.questadder.block

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.block.BlockBluePrint
import kor.toxicity.questadder.api.block.BlockType
import kor.toxicity.questadder.api.block.IQuestBlock
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import ru.beykerykt.minecraft.lightapi.common.LightAPI

class QuestBlock(private val blockKey: String, section: ConfigurationSection): IQuestBlock {
    private val type = section.getString("type")?.let {
        try {
            BlockType.valueOf(it.uppercase())
        } catch (ex: Exception) {
            QuestAdderBukkit.warn("block type \"$it\" doesn't exist.")
            null
        }
    } ?: BlockType.NOTE_BLOCK

    private val hash = section.getString("hash")?.let {
        try {
            it.toInt()
        } catch (ex: Exception) {
            QuestAdderBukkit.warn("$it is not an integer.")
            null
        }
    } ?: blockKey.hashCode()

    private val print = BlockBluePrint(
        section.getInt("light"),
        type,
        section.getBoolean("can-burned")
    )
    val data = when (type) {
        BlockType.NOTE_BLOCK -> NoteBlockData.fromHash(hash)
        BlockType.STRING -> StringBlockData.fromHash(hash)
        BlockType.CHORUS_PLANT -> ChorusPlantBlockData.fromHash(hash)
        BlockType.RED_MUSHROOM_BLOCK -> RedMushroomBlockData.fromHash(hash)
        BlockType.BROWN_MUSHROOM_BLOCK -> BrownMushroomBlockData.fromHash(hash)
        BlockType.MUSHROOM_STEM -> MushroomStemBlockData.fromHash(hash)
        BlockType.FIRE -> FireBlockData.fromHash(hash)
    }
    private val blockData = data.createBlockData()
    override fun place(location: Location) {
        val b = location.block
        b.setBlockData(blockData,false)
        if (print.light != 0 && Bukkit.getPluginManager().isPluginEnabled("LightAPI")) {
            try {
                val get = LightAPI.get().getLightLevel(location.world.name,location.blockX,location.blockY,location.blockZ)
                LightAPI.get().setLightLevel(location.world.name,location.blockX,location.blockY,location.blockZ,get + print.light)
            } catch (ex: Throwable) {
                QuestAdderBukkit.warn("An error has occurred while using LightAPI.")
            }
        }
    }

    override fun getBluePrint(): BlockBluePrint {
        return print
    }

    override fun getKey(): String {
        return blockKey
    }
}