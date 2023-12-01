package kor.toxicity.questadder.hooker.npc

import de.oliver.fancynpcs.api.Npc
import kor.toxicity.questadder.api.npc.FancyNpcsNPC
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*

class FancyNpcsNPCWrapper(val npc: Npc): FancyNpcsNPC {
    private val id = try {
        UUID.fromString(npc.data.id)
    } catch (ex: Exception) {
        UUID.randomUUID()
    }
    override fun getUUID(): UUID {
        return id
    }

    override fun getHandle(): Any {
        return npc
    }

    override fun getLocation(): Location? {
        return npc.data.location
    }

    override fun getWorld(): World? {
        return npc.data.location.world
    }
    override fun getEyeHeight(): Float {
        return npc.eyeHeight
    }

    override fun getEntity(): Entity? {
        return null
    }

    override fun getId(): String {
        return npc.data.id
    }
}
