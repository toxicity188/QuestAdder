package kor.toxicity.questadder.mechanic.npc

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.gui.IGui
import kor.toxicity.questadder.api.mechanic.IActualNPC
import kor.toxicity.questadder.api.mechanic.IQuestNPC
import kor.toxicity.questadder.api.mechanic.QuestRecord
import kor.toxicity.questadder.api.npc.WrappedNPC
import kor.toxicity.questadder.api.util.SoundData
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.nms.VirtualEntity
import kor.toxicity.questadder.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Supplier

class ActualNPC(val npc: WrappedNPC, val questNPC: QuestNPC): IActualNPC {

    private val playerDisplayMap = HashMap<UUID,PlayerDisplay>()

    private var task: ScheduledTask? = null

    override fun getLocationSupplier(): Supplier<Location>? {
        return npc.location?.let {
            Supplier {
                it.clone()
            }
        }
    }

    init {
        startTask()
    }
    fun cancel() {
        playerDisplayMap.values.forEach {
            it.remove()
        }
        playerDisplayMap.clear()
        task?.cancel()
        task = null
    }

    fun startTask() {
        cancel()
        task = QuestAdderBukkit.asyncTaskTimer(questNPC.thread,questNPC.thread) {
            val players = Bukkit.getOnlinePlayers().filter {
                it.world == npc.world && (npc.location ?: return@asyncTaskTimer).distance(it.location) <= questNPC.renderDistance
            }.toMutableSet()
            val iterator = playerDisplayMap.values.iterator()
            val task = ArrayList<Pair<Location, () -> Unit>>()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (!players.remove(next.player)) {
                    next.remove()
                    iterator.remove()
                } else {
                    task.add(next.player.location to {
                        next.update()
                    })
                }
            }
            players.forEach {
                QuestAdderBukkit.getPlayerData(it)?.let { data ->
                    playerDisplayMap.put(it.uniqueId,PlayerDisplay(it,data))?.remove()
                }
            }
            if (task.isNotEmpty()) task.forEach {
                QuestAdderBukkit.task(it.first, it.second)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActualNPC

        return npc == other.npc
    }

    override fun hashCode(): Int {
        return npc.hashCode()
    }

    override fun getSoundData(): SoundData {
        return questNPC.soundData
    }

    override fun getTalkerName(): String {
        return questNPC.name
    }

    override fun getTypingSpeed(): Long {
        return questNPC.typingSpeed
    }

    override fun getGui(): IGui? {
        return questNPC.inventory
    }

    private inner class PlayerDisplay(val player: Player, val data: PlayerData) {
        private var state: State? = null
        private var thread: EntityThread? = null
        private fun getState() = data.npcIndexes[questNPC.npcKey]?.let {
            questNPC.indicate[it]?.let { quest ->
                data.questVariables[quest.questKey]?.state?.let { state ->
                    when (state) {
                        QuestRecord.READY_TO_REQUEST -> State.READY_TO_REQUEST
                        QuestRecord.HAS ->  if (quest.isCompleted(player)) State.COMPLETE else State.HAS
                        QuestRecord.COMPLETE -> State.ALREADY_COMPLETED
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
                val loc = npc.location?.clone()?.apply {
                    y += npc.eyeHeight
                } ?: return
                thread = when (state) {
                    State.COMPLETE -> EntityThread(QuestAdderBukkit.nms.createArmorStand(player,loc).apply {
                        setText(Component.empty())
                        setItem(ItemStack(QuestAdderBukkit.Config.defaultResourcePackItem).apply {
                            itemMeta = itemMeta?.apply {
                                setCustomModelData(4)
                            }
                        })
                        setCustomNameVisible(false)
                    })
                    State.READY_TO_REQUEST -> EntityThread(QuestAdderBukkit.nms.createArmorStand(player,loc).apply {
                        setText(Component.empty())
                        setItem(ItemStack(QuestAdderBukkit.Config.defaultResourcePackItem).apply {
                            itemMeta = itemMeta?.apply {
                                setCustomModelData(3)
                            }
                        })
                        setCustomNameVisible(false)
                    })
                    else -> null
                }
            }
        }

        private inner class EntityThread(val entity: VirtualEntity) {

            private val task = QuestAdderBukkit.asyncTaskTimer(1,1) {
                entity.teleport((npc.location?.clone()?.apply {
                    y += npc.eyeHeight
                } ?: return@asyncTaskTimer).apply {
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

    override fun toUsedNPC(): WrappedNPC {
        return npc
    }

    override fun toQuestNPC(): IQuestNPC {
        return questNPC
    }
}
