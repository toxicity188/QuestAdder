package kor.toxicity.questadder.hooker.npc

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.npc.CitizensNPC
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*

class CitizensNPCWrapper(private val npc: NPC): CitizensNPC {
    override fun getUUID(): UUID {
        return npc.uniqueId
    }

    override fun getHandle(): Any {
        return npc
    }

    override fun getLocation(): Location? {
        return npc.entity?.location
    }

    override fun getWorld(): World? {
        return npc.entity?.world
    }

    override fun getEyeHeight(): Float {
        return npc.entity?.let {
            QuestAdderBukkit.nms.getEyeHeight(it)
        } ?: 0F
    }

    override fun getId(): Int {
        return npc.id
    }

    override fun getEntity(): Entity? {
        return npc.entity
    }
}
