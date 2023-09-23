package kor.toxicity.questadder.block

import org.bukkit.Instrument
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.NoteBlock

data class NoteBlockData(val powered: Boolean, val instrument: Instrument, val note: Note): QuestBlockData {
    companion object {
        fun fromHash(hash: Int): NoteBlockData {
            val powered = hash > 0
            val bit = hash and ((1 shl 16) - 1)
            val left = bit ushr 8
            val right = bit and ((1 shl 8) - 1)
            val entry = Instrument.entries
            return NoteBlockData(powered, entry[left % entry.size], Note((right % 24) + 1))
        }
        fun fromBlock(blockData: NoteBlock): NoteBlockData {
            return NoteBlockData(
                blockData.isPowered,
                blockData.instrument,
                blockData.note
            )
        }
    }

    @Suppress("DEPRECATION")
    override fun toKey() = "powered=$powered,instrument=${instrument.name.lowercase()},note=${note.id}"
    override fun createBlockData(): BlockData {
        return (Material.NOTE_BLOCK.createBlockData() as NoteBlock).also {
            it.isPowered = powered
            it.instrument = instrument
            it.note = note
        }
    }
}