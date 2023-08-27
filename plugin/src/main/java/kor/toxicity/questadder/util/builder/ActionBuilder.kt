package kor.toxicity.questadder.util.builder

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.QuestAdderEvent
import kor.toxicity.questadder.extension.ANNOTATION_PATTERN
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.extension.info
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.nms.RuntimeCommand
import kor.toxicity.questadder.util.action.AbstractAction
import kor.toxicity.questadder.util.action.CancellableAction
import kor.toxicity.questadder.util.action.RegistrableAction
import kor.toxicity.questadder.util.action.type.*
import kor.toxicity.questadder.util.event.AbstractEvent
import kor.toxicity.questadder.util.event.type.*
import kor.toxicity.questadder.util.reflect.ActionReflector
import org.bukkit.command.CommandExecutor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.regex.Pattern

object ActionBuilder {

    private val actionPattern = Pattern.compile("^(?<name>([a-zA-Z]+))(?<argument>\\{[\\w|\\W]*})?$")
    private val delayPattern = Pattern.compile("delay (?<delay>[0-9]+)")

    private val actionMap = HashMap<String,Class<out AbstractAction>>().apply {
        put("message",ActMessage::class.java)
        put("title",ActTitle::class.java)

        put("slate",ActSlate::class.java)
        put("action",ActAction::class.java)
        put("quest",ActQuest::class.java)

        put("sound", ActSound::class.java)

        put("set",ActSet::class.java)
        put("remove",ActRemove::class.java)
        put("add",ActAdd::class.java)
        put("subtract",ActSubtract::class.java)
        put("multiply",ActMultiply::class.java)
        put("divide",ActDivide::class.java)
        put("index", ActIndex::class.java)

        put("warp", ActWarp::class.java)
        put("evaluate",ActEvaluate::class.java)

        put("give",ActGive::class.java)
        put("take",ActTake::class.java)

        put("command", ActCommand::class.java)
        put("money", ActMoney::class.java)
        put("cinematic", ActCinematic::class.java)
    }
    private val eventMap = HashMap<String,Class<out AbstractEvent<*>>>().apply {
        put("join", EventJoin::class.java)
        put("kill", EventKill::class.java)
        put("attack",EventAttack::class.java)
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

        put("navigatestart",EventNavigateStart::class.java)
        put("navigatefail",EventNavigateFail::class.java)
        put("navigateend",EventNavigateEnd::class.java)
        put("navigatecomplete",EventNavigateComplete::class.java)

        put("givereward", EventGiveReward::class.java)
        put("questgive", EventQuestGive::class.java)
        put("questremove", EventQuestRemove::class.java)
        put("questcomplete", EventQuestComplete::class.java)
        put("questsurrender", EventQuestSurrender::class.java)
        put("questsurrenderfail", EventQuestSurrenderFail::class.java)
        put("questselect", EventQuestSelect::class.java)
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
                    QuestAdder.warn("unable to load this action: $parameter")
                    QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    null
                }
            }
        }
    }
    fun createEvent(adder: QuestAdder, action: AbstractAction, parameter: String): AbstractEvent<*>? {
        return match(parameter)?.let { p ->
            eventMap[p.first]?.let {
                try {
                    ActionReflector(
                        it.getConstructor(QuestAdder::class.java,AbstractAction::class.java).newInstance(adder,action),
                        p.second
                    ).result
                } catch (ex: Exception) {
                    QuestAdder.warn("unable to load this event: $parameter")
                    QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    null
                }
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
        val empty: CancellableAction = object : CancellableAction(adder) {
            override fun invoke(player: Player, event: QuestAdderEvent) {
                playerTask.remove(player.uniqueId)
            }

            override fun cancel(player: Player) {

            }
        }
        var action = empty
        for (parameter in parameters.reversed()) {
            val t = action
            val matcher = delayPattern.matcher(parameter)
            if (matcher.find()) {
                val d = matcher.group("delay").toLong()
                action = object : CancellableAction(adder) {
                    override fun invoke(player: Player, event: QuestAdderEvent) {
                        playerTask[player.uniqueId] = QuestAdder.taskLater(d) {
                            t.invoke(player,event)
                        }
                    }

                    override fun cancel(player: Player) {
                        t.cancel(player)
                    }
                }
            } else {
                createAction(adder,parameter)?.let {
                    action = object : CancellableAction(adder) {
                        override fun invoke(player: Player, event: QuestAdderEvent) {
                            it.invoke(player,event)
                            t.invoke(player,event)
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
                playerTask.remove(player.uniqueId)?.cancel()
            }

            override fun invoke(player: Player, event: QuestAdderEvent) {
                action.invoke(player, event)
            }
        } else object : CancellableAction(adder) {
            override fun invoke(player: Player, event: QuestAdderEvent) {
                if (!playerTask.contains(player.uniqueId)) {
                    action.invoke(player, event)
                }
            }

            override fun cancel(player: Player) {
                playerTask.remove(player.uniqueId)?.cancel()
            }
        })
    }
    fun build(adder: QuestAdder, section: ConfigurationSection): RegistrableAction? {
        val unsafe = section.getBoolean("Unsafe")
        return section.findStringList("Action","Actions","actions","action")?.let {
            create(adder,it,unsafe)
        }?.let { action ->
            var predicate: (Player,QuestAdderEvent) -> Boolean = { _, _ ->
                true
            }
            section.findStringList("Event","Events","event","events")?.forEach {
                createEvent(adder,action,it)
            }
            section.findStringList("Condition","Conditions","conditions","condition")?.forEach { s ->
                val original = predicate
                val matcher = ANNOTATION_PATTERN.matcher(s)
                if (matcher.find()) {
                    val function = FunctionBuilder.evaluate(matcher.replaceAll(""))
                    val name = matcher.group("name")
                    val value = matcher.group("value")
                    when (name) {
                        "cast" -> {
                            adder.addLazyTask {
                                val castActions = value.split(',').mapNotNull {
                                    DialogManager.getAction(it)
                                }
                                if (castActions.isNotEmpty()) {
                                    predicate = { player, event ->
                                        val get = function.apply(event)
                                        original(player,event) && if (get is Boolean) {
                                            if (get) {
                                                castActions.random().invoke(player, event)
                                                true
                                            } else false
                                        } else false
                                    }
                                }
                            }
                        }
                        "castinstead" -> {
                            adder.addLazyTask {
                                val castActions = value.split(',').mapNotNull {
                                    DialogManager.getAction(it)
                                }
                                if (castActions.isNotEmpty()) {
                                    predicate = { player, event ->
                                        val get = function.apply(event)
                                        original(player,event) && if (get is Boolean) {
                                            if (get) {
                                                castActions.random().invoke(player, event)
                                                false
                                            } else true
                                        } else false
                                    }
                                }
                            }
                        }
                        else -> {
                            predicate = { player, event ->
                                val get = function.apply(event)
                                original(player,event) && get is Boolean && get
                            }
                        }
                    }
                } else {
                    val function = FunctionBuilder.evaluate(s)
                    predicate = { player, event ->
                        val get = function.apply(event)
                        original(player,event) && get is Boolean && get
                    }
                }
            }
            val obj = object : CancellableAction(adder) {
                override fun invoke(player: Player, event: QuestAdderEvent) {
                    if (predicate(player,event)) action.invoke(player,event)
                }

                override fun cancel(player: Player) {
                    action.cancel(player)
                }
            }
            val command = ArrayList<RuntimeCommand>()
            section.findStringList("Command","command","commands","Commands")?.let { sl ->
                val executor = CommandExecutor { sender, _, _, args ->
                    if (sender !is Player) sender.info("sorry, this command is player only.")
                    else obj.apply(sender,*args)
                    true
                }
                sl.forEach {
                    command.add(QuestAdder.nms.createCommand(it.split(' ')[0],executor))
                }
            }
            object : RegistrableAction(adder) {
                override fun invoke(player: Player, event: QuestAdderEvent) {
                    obj.invoke(player,event)
                }
                override fun cancel(player: Player) {
                    obj.cancel(player)
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