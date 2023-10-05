package kor.toxicity.questadder.util.builder

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.QuestAdder
import kor.toxicity.questadder.api.event.ActionCancelEvent
import kor.toxicity.questadder.api.event.QuestAdderEvent
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.nms.RuntimeCommand
import kor.toxicity.questadder.api.mechanic.AbstractAction
import kor.toxicity.questadder.api.mechanic.CancellableAction
import kor.toxicity.questadder.api.mechanic.RegistrableAction
import kor.toxicity.questadder.api.mechanic.AbstractEvent
import kor.toxicity.questadder.api.mechanic.ActionResult
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.UUIDManager
import kor.toxicity.questadder.util.action.*
import kor.toxicity.questadder.util.event.*
import kor.toxicity.questadder.util.reflect.ActionReflector
import kor.toxicity.questadder.util.reflect.PrimitiveType
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.collections.HashMap

object ActionBuilder {

    private val actionPattern = Pattern.compile("^(?<name>([a-zA-Z]+))(?<argument>\\{[\\w|\\W]*})?$")
    private val delayPattern = Pattern.compile("delay (?<delay>[0-9]+)")

    private val playerTaskMap = ConcurrentHashMap<UUID,MutableMap<UUID,CancellableAction>>()

    private val actionMap = HashMap<String,Class<out AbstractAction>>().apply {
        put("message", ActMessage::class.java)
        put("title", ActTitle::class.java)

        put("slate", ActSlate::class.java)
        put("action", ActAction::class.java)
        put("quest", ActQuest::class.java)

        put("sound", ActSound::class.java)

        put("set", ActSet::class.java)
        put("remove", ActRemove::class.java)
        put("add", ActAdd::class.java)
        put("subtract", ActSubtract::class.java)
        put("multiply", ActMultiply::class.java)
        put("divide", ActDivide::class.java)
        put("index", ActIndex::class.java)

        put("warp", ActWarp::class.java)
        put("evaluate", ActEvaluate::class.java)

        put("give", ActGive::class.java)
        put("take", ActTake::class.java)

        put("command", ActCommand::class.java)
        put("money", ActMoney::class.java)
        put("cinematic", ActCinematic::class.java)

        put("randomaction", ActRandomAction::class.java)

        put("startnavigate", ActStartNavigate::class.java)
        put("endnavigate", ActEndNavigate::class.java)

        put("input", ActInput::class.java)
        put("dialog", ActDialog::class.java)
        put("customdialog", ActCustomDialog::class.java)

        put("stopsound", ActStopSound::class.java)

        put("potion",ActPotion::class.java)
        put("clearpotion",ActClearPotion::class.java)

        put("clone", ActClone::class.java)
        put("removeentity", ActRemoveEntity::class.java)
    }
    private val eventMap = HashMap<String,Class<out AbstractEvent<*>>>().apply {
        put("join", EventJoin::class.java)
        put("kill", EventKill::class.java)
        put("attack", EventAttack::class.java)
        put("chat", EventChat::class.java)
        put("talk", EventTalk::class.java)
        put("quit", EventQuit::class.java)
        put("command", EventCommand::class.java)
        put("walk", EventWalk::class.java)

        put("fish", EventFish::class.java)
        put("animation", EventAnimation::class.java)
        put("changeworld", EventChangeWorld::class.java)
        put("entityclick", EventEntityClick::class.java)
        put("npcclick", EventNPCClick::class.java)

        put("blockclick", EventBlockClick::class.java)
        put("blockbreak", EventBlockBreak::class.java)
        put("blockplace", EventBlockPlace::class.java)

        put("respawn", EventRespawn::class.java)
        put("sneak", EventSneak::class.java)
        put("sprint", EventSprint::class.java)

        put("itemclick", EventItemClick::class.java)

        put("resourecpack", EventResourcePack::class.java)

        put("navigatestart", EventNavigateStart::class.java)
        put("navigatefail", EventNavigateFail::class.java)
        put("navigateend", EventNavigateEnd::class.java)
        put("navigatecomplete", EventNavigateComplete::class.java)

        put("givereward", EventGiveReward::class.java)
        put("questgive", EventQuestGive::class.java)
        put("questremove", EventQuestRemove::class.java)
        put("questcomplete", EventQuestComplete::class.java)
        put("questsurrender", EventQuestSurrender::class.java)
        put("questsurrenderfail", EventQuestSurrenderFail::class.java)
        put("questselect", EventQuestSelect::class.java)
    }

    fun cancelAll(player: Player, reason: ActionCancelEvent.CancelReason) {
        var cancelled = false
        playerTaskMap.remove(player.uniqueId)?.forEach {
            it.value.cancel(player)
            cancelled = true
        }
        if (cancelled) ActionCancelEvent(player, reason).call()
    }

    fun addAction(name: String, clazz: Class<out AbstractAction>) {
        actionMap.putIfAbsent(name.lowercase(),clazz)
    }
    fun addEvent(name: String, clazz: Class<out AbstractEvent<*>>) {
        eventMap.putIfAbsent(name.lowercase(),clazz)
    }

    fun createAction(adder: QuestAdder, parameter: String): AbstractAction? {
        return match(parameter)?.let { p ->
            actionMap[p.first]?.let {
                try {
                    ActionReflector(
                        it.getConstructor(QuestAdder::class.java).newInstance(adder),
                        p.second
                    ).result
                } catch (ex: Exception) {
                    QuestAdderBukkit.warn("unable to load this action: $parameter")
                    QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    null
                }
            } ?: run {
                QuestAdderBukkit.warn("no action found: ${p.first}")
                null
            }
        }
    }
    fun createEvent(adder: QuestAdder, action: AbstractAction, parameter: String): AbstractEvent<*>? {
        return match(parameter)?.let { p ->
            eventMap[p.first]?.let {
                try {
                    ActionReflector(
                        it.getConstructor(QuestAdder::class.java,
                            AbstractAction::class.java).newInstance(adder,action),
                        p.second
                    ).result
                } catch (ex: Exception) {
                    QuestAdderBukkit.warn("unable to load this event: $parameter")
                    QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    null
                }
            } ?: run {
                QuestAdderBukkit.warn("no event found: ${p.first}")
                null
            }
        }
    }
    private fun match(parameter: String): Pair<String,JsonObject>? {
        val matcher = actionPattern.matcher(parameter)
        return if (matcher.find()) {
            val name = matcher.group("name").lowercase()
            val argument = matcher.group("argument")
            name to if (argument != null) JsonParser.parseString(argument).asJsonObject else JsonObject()
        }
        else null
    }

    fun create(adder: QuestAdder, parameters: Collection<String>, unsafe: Boolean = false): CancellableAction? {
        val playerTask = HashMap<UUID,BukkitTask>()
        val uuid = UUIDManager.createRandomUUID()
        val empty: CancellableAction = object : CancellableAction(adder) {
            override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                val playerUUID = player.uniqueId
                playerTask.remove(playerUUID)
                playerTaskMap[playerUUID]?.let {
                    it.remove(uuid)
                    if (it.isEmpty()) playerTaskMap.remove(playerUUID)
                }
                return ActionResult.SUCCESS
            }

            override fun cancel(player: Player) {

            }
        }
        var action = empty
        var delay = 0L
        for ((index,parameter) in parameters.reversed().withIndex()) {
            val t = action
            val matcher = delayPattern.matcher(parameter)
            if (matcher.find()) {
                val d = matcher.group("delay").toLong()
                action = object : CancellableAction(adder) {
                    override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                        playerTask[player.uniqueId] = QuestAdderBukkit.taskLater(d) {
                            t.invoke(player,event)
                        }
                        return ActionResult.SUCCESS
                    }

                    override fun cancel(player: Player) {
                        t.cancel(player)
                    }
                }
                delay += d
            } else {
                createAction(adder,parameter)?.let {
                    action = object : CancellableAction(adder) {
                        override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                            if (it.invoke(player,event) == ActionResult.FAIL) {
                                QuestAdderBukkit.platform.kick(player, "An error has occurred while playing action line ${parameter.length - index}".asClearComponent())
                                return ActionResult.FAIL
                            }
                            return t.invoke(player,event)
                        }

                        override fun cancel(player: Player) {
                            if (it is CancellableAction) it.cancel(player)
                            t.cancel(player)
                        }
                    }
                }
            }
        }
        return if (action === empty) null else (if (unsafe) object : CancellableAction(adder) {
            override fun cancel(player: Player) {
                val playerUUID = player.uniqueId
                playerTask.remove(playerUUID)?.cancel()
                playerTaskMap[playerUUID]?.let {
                    it.remove(uuid)
                    if (it.isEmpty()) playerTaskMap.remove(playerUUID)
                }
            }

            override fun getRunningTime(): Long {
                return delay
            }

            override fun isUnsafe(): Boolean {
                return true
            }

            override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                return action.invoke(player, event)
            }
        } else object : CancellableAction(adder) {
            override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                if (!playerTask.contains(player.uniqueId)) {
                    playerTaskMap.getOrPut(player.uniqueId) {
                        HashMap()
                    }[uuid] = this
                    return action.invoke(player, event)
                }
                return ActionResult.ALREADY_EXECUTED
            }

            override fun getRunningTime(): Long {
                return delay
            }

            override fun isUnsafe(): Boolean {
                return false
            }

            override fun cancel(player: Player) {
                playerTask.remove(player.uniqueId)?.cancel()
            }
        })
    }
    fun build(adder: QuestAdder, section: ConfigurationSection): RegistrableAction? {
        val unsafe = section.findBoolean("unsafe","Unsafe")
        return section.findStringList("Action","Actions","actions","action")?.let {
            create(adder,it,unsafe)
        }?.let { action ->
            var predicate: (Player, QuestAdderEvent) -> ActionResult = { _, _ ->
                ActionResult.SUCCESS
            }
            section.findStringList("Condition","Conditions","conditions","condition")?.forEach { s ->
                val matcher = ANNOTATION_PATTERN.matcher(s)
                if (matcher.find()) {
                    val name = matcher.group("name")
                    val value = matcher.group("value")
                    val function = FunctionBuilder.evaluate(matcher.replaceAll(""))
                    val retType = function.getReturnType()
                    if (retType != PrimitiveType.BOOLEAN.primitive && retType != PrimitiveType.BOOLEAN.reference) {
                        QuestAdderBukkit.warn("compile error: this argument is not a boolean: $s")
                        return@forEach
                    }
                    when (name) {
                        "cast" -> {
                            adder.addLazyTask {
                                val original = predicate
                                val castActions = value.split(',').mapNotNull {
                                    DialogManager.getAction(it)
                                }
                                if (castActions.isNotEmpty()) {
                                    predicate = { player, event ->
                                        val get = function.apply(event)
                                        val originalResult = original(player,event)
                                        if (get as Boolean) {
                                            castActions.random().invoke(player, event)
                                            ActionResult.SUCCESS
                                        } else originalResult
                                    }
                                } else QuestAdderBukkit.warn("unable to load the dialog: $s")
                            }
                        }
                        "castinstead" -> {
                            adder.addLazyTask {
                                val original = predicate
                                val castActions = value.split(',').mapNotNull {
                                    DialogManager.getAction(it)
                                }
                                if (castActions.isNotEmpty()) {
                                    predicate = { player, event ->
                                        val get = function.apply(event)
                                        val originalResult = original(player,event)
                                        if (originalResult == ActionResult.SUCCESS && get as Boolean) {
                                            val otherResult = castActions.random().invoke(player, event)
                                            if (otherResult == ActionResult.SUCCESS) ActionResult.INSTEAD_OTHER_ACTION else otherResult
                                        } else originalResult
                                    }
                                } else QuestAdderBukkit.warn("unable to load the dialog: $s")
                            }
                        }
                        else -> {
                            val original = predicate
                            predicate = { player, event ->
                                val get = function.apply(event)
                                if (original(player,event) == ActionResult.SUCCESS && get as Boolean) ActionResult.SUCCESS else ActionResult.UNMET_CONDITION
                            }
                        }
                    }
                } else {
                    val original = predicate
                    val function = FunctionBuilder.evaluate(s)
                    val retType = function.getReturnType()
                    if (retType != PrimitiveType.BOOLEAN.primitive && retType != PrimitiveType.BOOLEAN.reference) {
                        QuestAdderBukkit.warn("compile error: this argument is not a boolean: $s")
                        return@forEach
                    }
                    predicate = { player, event ->
                        val get = function.apply(event)
                        if (original(player,event) == ActionResult.SUCCESS && get as Boolean) ActionResult.SUCCESS else ActionResult.UNMET_CONDITION
                    }
                }
            }
            val obj = object : CancellableAction(adder) {
                override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                    return when (val result = predicate(player,event)) {
                        ActionResult.SUCCESS -> action.invoke(player,event)
                        else -> result
                    }
                }

                override fun getRunningTime(): Long {
                    return action.runningTime
                }

                override fun isUnsafe(): Boolean {
                    return action.isUnsafe
                }

                override fun cancel(player: Player) {
                    action.cancel(player)
                }
            }
            section.findStringList("Event","Events","event","events")?.forEach {
                createEvent(adder,obj,it)
            }
            val command = ArrayList<RuntimeCommand>()
            section.findStringList("Command","command","commands","Commands")?.let { sl ->
                val executor = CommandExecutor { sender, _, _, args ->
                    if (sender !is Player) sender.info("sorry, this command is player only.")
                    else obj.apply(sender,*args)
                    true
                }
                sl.forEach {
                    command.add(QuestAdderBukkit.nms.createCommand(it.split(' ')[0],executor))
                }
            }
            object : RegistrableAction(adder) {
                override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                    return obj.invoke(player,event)
                }
                override fun cancel(player: Player) {
                    obj.cancel(player)
                }
                override fun getRunningTime(): Long {
                    return obj.runningTime
                }

                override fun isUnsafe(): Boolean {
                    return obj.isUnsafe
                }

                override fun unregister() {
                    command.forEach {
                        it.unregister()
                    }
                }
            }
        }

    }
}
