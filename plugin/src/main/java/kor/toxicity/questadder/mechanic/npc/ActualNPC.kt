package kor.toxicity.questadder.mechanic.npc

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.mechanic.quest.QuestState
import kor.toxicity.questadder.nms.VirtualEntity
import net.citizensnpcs.api.npc.NPC
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.UUID

class ActualNPC(val npc: NPC, val questNPC: QuestNPC) {

    private val playerDisplayMap = HashMap<UUID,PlayerDisplay>()

    private val thread = QuestAdder.asyncTaskTimer(questNPC.thread,questNPC.thread) {
        val entity = npc.entity
        val loc = entity.location
        val players = Bukkit.getOnlinePlayers().filter {
            it.world == entity.world && loc.distance(it.location) <= questNPC.renderDistance
        }.toMutableSet()
        val iterator = playerDisplayMap.values.iterator()
        val task = ArrayList<() -> Unit>()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (!players.remove(next.player)) {
                next.remove()
                iterator.remove()
            } else {
                task.add {
                    next.update()
                }
            }
        }
        players.forEach {
            QuestAdder.getPlayerData(it)?.let { data ->
                playerDisplayMap.put(it.uniqueId,PlayerDisplay(it,data))?.remove()
            }
        }
        if (task.isNotEmpty()) QuestAdder.task {
            task.forEach {
                it()
            }
        }
    }
    @Internal
    fun cancel() {
        playerDisplayMap.values.forEach {
            it.remove()
        }
        thread.cancel()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActualNPC

        if (npc != other.npc) return false

        return true
    }

    override fun hashCode(): Int {
        return npc.hashCode()
    }

    private inner class PlayerDisplay(val player: Player, val data: PlayerData) {
        private var state: State? = null
        private var thread: EntityThread? = null
        private fun getState() = data.npcIndexes[questNPC.key]?.let {
            questNPC.indicate[it]?.let { quest ->
                data.questVariables[quest.key]?.state?.let { state ->
                    when (state) {
                        QuestState.HAS ->  if (quest.isCompleted(player)) State.COMPLETE else State.HAS
                        QuestState.COMPLETE -> State.ALREADY_COMPLETED
                    }
                } ?: State.READY_TO_REQUEST
            }
        } ?: State.NOT_EXIST

        fun remove() {
            thread?.remove()
        }
        fun update() {
            val newState = getState()
            if (state != newState) {
                state = newState
                thread?.remove()
                thread = when (state) {
                    State.COMPLETE -> EntityThread(QuestAdder.nms.createArmorStand(player,npc.entity.location).apply {
                        setText(Component.empty())
                        setItem(ItemStack(QuestAdder.Config.defaultResourcePackItem).apply {
                            itemMeta = itemMeta?.apply {
                                setCustomModelData(4)
                            }
                        })
                    })
                    State.READY_TO_REQUEST -> EntityThread(QuestAdder.nms.createArmorStand(player,npc.entity.location).apply {
                        setText(Component.empty())
                        setItem(ItemStack(QuestAdder.Config.defaultResourcePackItem).apply {
                            itemMeta = itemMeta?.apply {
                                setCustomModelData(3)
                            }
                        })
                    })
                    else -> null
                }
            }
        }

        private inner class EntityThread(val entity: VirtualEntity) {

            private val task = QuestAdder.asyncTaskTimer(1,1) {
                entity.teleport(npc.entity.location.apply {
                    pitch = 0F
                    yaw = player.location.yaw - 180F
                })
            }
            fun remove() {
                task.cancel()
                entity.remove()
            }
        }
    }
    private enum class State {
        ALREADY_COMPLETED,
        COMPLETE,
        HAS,
        READY_TO_REQUEST,
        NOT_EXIST
    }


}