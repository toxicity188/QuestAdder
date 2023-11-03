package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import java.util.UUID

object UUIDManager: QuestAdderManager {

    private val uuidRegistry = HashSet<UUID>()

    override fun start(adder: QuestAdderBukkit) {
    }

    override fun reload(adder: QuestAdderBukkit) {
        uuidRegistry.clear()
    }

    override fun end(adder: QuestAdderBukkit) {
    }

    fun createRandomUUID(): UUID {
        var uuid = UUID.randomUUID()
        while (!uuidRegistry.add(uuid)) uuid = UUID.randomUUID()
        return uuid
    }
}
