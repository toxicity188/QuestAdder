package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.QuestAdder
import kor.toxicity.questadder.api.event.DialogStartEvent
import kor.toxicity.questadder.api.mechanic.IDialog
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.manager.GestureManager
import kor.toxicity.questadder.manager.SlateManager
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.nms.VirtualArmorStand
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.SoundData
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.gui.Gui
import kor.toxicity.questadder.util.gui.GuiData
import kor.toxicity.questadder.util.gui.GuiExecutor
import kor.toxicity.questadder.util.gui.MouseButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.title.Title
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.collections.HashMap

class Dialog(adder: QuestAdder, file: File, val dialogKey: String, section: ConfigurationSection): IDialog {
    private interface TypingExecutor {
        fun initialize(talker: Component?)
        fun run(talk: Component)
        fun end()
    }
    private interface TypingManager {
        fun create(current: DialogCurrent): TypingExecutor
    }

    private class DialogCurrent(
        val player: Player,
        val npc: ActualNPC,
        val event: DialogStartEvent,
        var run: DialogRun
    ) {
        var executor: TypingExecutor? = null
        var typingSpeed = QuestAdderBukkit.Config.defaultTypingSpeed
        val typingSoundMap = HashMap<String,SoundData>()
        var inventory: Gui.GuiHolder? = null
        var display: VirtualArmorStand? = null
        var safeEnd = false
    }
    private inner class DialogRun {

        val current: DialogCurrent

        constructor(player: Player, questNPC: ActualNPC) {
            this.current = DialogCurrent(player, questNPC, DialogStartEvent(
                player,
                questNPC,
                this@Dialog
            ).apply {
                callEvent()
            },this)
        }
        constructor(dialogCurrent: DialogCurrent) {
            this.current = dialogCurrent.also {
                it.run = this
            }
        }
        private var talkIndex = 0
        private var talkerComponent: Component? = null
        private var talkComponent = Component.empty()
        private var task: BukkitTask? = null
        private var iterator = ComponentReader.emptyIterator()

        private var started = false

        fun start() {
            cancel()
            if (talkIndex < talk.size) {
                this@Dialog.executorMap[talkIndex + 1]?.let { d ->
                    current.executor?.end()
                    current.executor = d.create(current)
                }
                if (current.executor == null) current.executor = defaultExecutor.create(current)
                started = true
                talkComponent = Component.empty()
                talkerComponent = talker[talkIndex + 1]?.createComponent(current.event)
                current.executor!!.initialize(talkerComponent)
                iterator = talk[talkIndex].createIterator(current.event) ?: ComponentReader.errorIterator()
                talkTask[talkIndex + 1]?.let { g ->
                    g(current)
                }
                startTask()
                talkIndex++
            } else {
                end()
            }
        }
        private fun cancel() {
            task?.cancel()
        }
        fun stop() {
            cancel()
            playerTask.remove(current.player.uniqueId)
        }
        fun end() {
            stop()
            lastAction(current)
            dialog?.let {
                if (it.isNotEmpty()) {
                    it.random().start(current)
                    return
                }
            }
            current.safeEnd = true
            current.executor?.end()
            qna?.let {
                if (it.isNotEmpty()) {
                    it.random().open(
                        current.player,
                        current.event,
                        current.inventory?.data?.gui?.name ?: Component.empty(),
                        talkerComponent ?: current.npc.questNPC.name.asComponent().deepClear(),
                        if (talk.isNotEmpty()) talk.last().createComponent(current.event) ?: Component.empty() else null
                    )
                    return
                }
            }
            QuestAdderBukkit.task {
                current.player.closeInventory()
            }
        }

        fun restart() {
            if (!started) return
            cancel()
            startTask()
        }

        private fun startTask() {
            task = QuestAdderBukkit.taskTimer(current.typingSpeed,current.typingSpeed) {
                if (iterator.hasNext()) {
                    val next = iterator.nextLine()
                    if ((next as TextComponent).content() != "*") {
                        talkComponent = talkComponent.append(next)
                        (talkerComponent?.let {
                            current.typingSoundMap[it.onlyText()] ?: QuestAdderBukkit.Config.defaultTypingSound
                        } ?: current.npc.questNPC.soundData).play(current.player)
                        current.executor!!.run(talkComponent)
                    }
                } else {
                    cancel()
                    started = false
                    task = QuestAdderBukkit.taskLater(20) {
                        start()
                    }
                }
            }
        }
    }

    companion object {
        private val talkPattern = Pattern.compile("^((?<talker>(\\w|\\W)+)::)?(\\s+)?(?<talk>(\\w|\\W)+)$")
        private val playerTask = ConcurrentHashMap<UUID,DialogRun>()
        private val defaultExecutor = object : TypingManager {
            override fun create(current: DialogCurrent): TypingExecutor {
                val selectedInv = current.npc.questNPC.inventory ?: createInventory("talking with ${current.npc.questNPC.npcKey}".asComponent(),5)
                val inv = current.inventory?.apply {
                    current.player.openInventory(inventory)
                } ?: selectedInv.open(current.player, object :
                    GuiExecutor {
                    override fun end(data: GuiData) {
                        if (!current.safeEnd) {
                            current.run.stop()
                            SlateManager.slateOff(current.player)
                        }
                    }

                    override fun initialize(data: GuiData) {

                    }

                    override fun click(
                        data: GuiData,
                        clickedItem: ItemStack,
                        clickedSlot: Int,
                        isPlayerInventory: Boolean,
                        button: MouseButton
                    ) {
                        when (button) {
                            MouseButton.LEFT -> {
                                current.run.start()
                            }
                            MouseButton.SHIFT_LEFT -> {
                                current.typingSpeed = (current.typingSpeed - 1).coerceAtLeast(1)
                                current.run.restart()
                            }
                            MouseButton.RIGHT -> {
                                current.run.end()
                            }
                            MouseButton.SHIFT_RIGHT -> {
                                current.typingSpeed = (current.typingSpeed + 1).coerceAtMost(4)
                                current.run.restart()
                            }
                            else -> {}
                        }
                    }

                }).apply {
                    current.inventory = this
                }
                val item = ItemStack(QuestAdderBukkit.Config.defaultDialogItem)
                val meta = item.itemMeta

                return object : TypingExecutor {
                    override fun initialize(talker: Component?) {
                        item.itemMeta = meta.apply {
                            displayName((talker ?: current.npc.questNPC.name.asComponent().deepClear()).append(":".asComponent().deepClear()))
                        }
                        for (i in 0..44) {
                            inv.inventory.setItem(i,null)
                        }
                        selectedInv.items.forEach {
                            inv.inventory.setItem(it.key,it.value)
                        }

                        inv.inventory.setItem(22, item)
                        current.player.updateInventory()
                    }

                    override fun run(talk: Component) {
                        item.itemMeta = meta.apply {
                            lore(listOf(talk))
                        }
                        inv.inventory.setItem(22, item)
                        current.player.updateInventory()
                    }

                    override fun end() {
                        if (!current.safeEnd) {
                            current.safeEnd = true
                            QuestAdderBukkit.task {
                                current.player.closeInventory()
                                current.inventory = null
                                current.safeEnd = false
                            }
                        }
                    }
                }
            }
        }
        fun stop(player: Player) = playerTask.remove(player.uniqueId)?.stop()
        fun isRunning(player: Player) = playerTask.containsKey(player.uniqueId)

        private val typingManagerMap = HashMap<String,TypingManager>().apply {
            put("default", defaultExecutor)
            put("title", object : TypingManager {
                override fun create(current: DialogCurrent): TypingExecutor {
                    return object : TypingExecutor {
                        private var talker: Component = Component.empty()

                        override fun run(talk: Component) {
                            current.player.showTitle(Title.title(
                                talker,
                                talk,
                                Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofSeconds(2),
                                    Duration.ofSeconds(1)
                                )
                            ))
                        }
                        override fun initialize(talker: Component?) {
                            this.talker = talker ?: current.npc.questNPC.name.asComponent().deepClear()
                        }
                        override fun end() {
                        }
                    }
                }
            })
            put("text", object : TypingManager {
                override fun create(current: DialogCurrent): TypingExecutor {
                    return object : TypingExecutor {

                        private var referencedEntity = current.npc.npc.entity
                        private val display = current.display ?: QuestAdderBukkit.nms.createArmorStand(current.player,referencedEntity.location).apply {
                            setText(Component.empty())
                            current.display = this
                        }
                        override fun end() {
                            display.remove()
                            current.display = null
                        }

                        override fun initialize(talker: Component?) {
                            referencedEntity = if (talker != null && talker.onlyText() == "player") {
                                current.player
                            }
                            else current.npc.npc.entity
                        }

                        override fun run(talk: Component) {
                            display.setText(talk)
                            display.teleport(referencedEntity.location.apply {
                                y += 0.25
                            })
                        }
                    }
                }
            })
        }
    }
    private val talk = ArrayList<ComponentReader<DialogStartEvent>>()
    private val talker = HashMap<Int,ComponentReader<DialogStartEvent>>()
    private var dialog: MutableList<Dialog>? = null
    private var qna: MutableList<QnA>? = null
    private var subDialog: MutableList<Dialog>? = null
    private var executorMap = HashMap<Int,TypingManager>()
    private var predicate: (DialogCurrent) -> Boolean = {
        true
    }
    private var lastAction: (DialogCurrent) -> Unit = {}
    private var talkTask = HashMap<Int,(DialogCurrent) -> Unit>()
    private val typingSound = HashMap<String,SoundData>().apply {
        section.findConfig("TypingSound","typing-sound")?.let { config ->
            config.getKeys(false).forEach {
                config.getAsSoundData(it)?.let { sound ->
                    put(it,sound)
                } ?: QuestAdderBukkit.warn("syntax error: unable to find sound data: $it ($dialogKey in ${file.name})")
            }
        }
    }

    /**
     * Add the action be called when this index of talk starts.
     *
     * @param index the index of talk
     * @param block the function be called.
     * @since 1.0
     */
    fun addTalkAction(index: Int, block: (Player,ActualNPC) -> Unit) {
        val act = talkTask[index] ?: {}
        talkTask[index] = {
            act(it)
            block(it.player,it.npc)
        }
    }
    /**
     * Add the action be called when this dialog ends.
     *
     * @param block the function be called.
     * @since 1.0
     */
    fun addLastAction(block: (Player,ActualNPC) -> Unit) {
        val act = lastAction
        lastAction = {
            act(it)
            block(it.player,it.npc)
        }
    }

    private fun addTalkTask(i: Int, action: (DialogCurrent) -> Unit) {
        val act = talkTask[i] ?: {}
        talkTask[i] = {
            act(it)
            action(it)
        }
    }
    private fun addLastAction(action: (DialogCurrent) -> Unit) {
        val before = lastAction
        lastAction = {
            before(it)
            action(it)
        }
    }
    init {
        fun addPredicate(action: (DialogCurrent) -> Boolean) {
            val before = predicate
            predicate = {
                before(it) && action(it)
            }
        }
        fun error(message: String) {
            QuestAdderBukkit.warn("$message ($dialogKey in ${file.name})")
        }
        section.findStringList("talk","Talk")?.let { t ->
            t.forEach {
                val matcher = talkPattern.matcher(it)
                if (matcher.find()) {
                    talk.add(ComponentReader(matcher.group("talk")))
                    val talkerGroup = matcher.group("talker")
                    if (talkerGroup != null) talker[talk.size] = ComponentReader(talkerGroup)
                }
            }
        }
        section.findConfig("talker","Talker","sender","Sender")?.let { config ->
            config.getKeys(false).forEach {
                config.getString(it)?.let { s ->
                    try {
                        talker[it.toInt()] = ComponentReader(s)
                    } catch (ex: Exception) {
                        error("format error: $it is not a number.")
                    }
                } ?: error("syntax error: the key '$it' is not a string.")
            }
        }
        section.findStringList("dialog","Dialog","LinkedDialog","linked-dialog")?.let { t ->
            adder.addLazyTask {
                t.forEach {
                    DialogManager.getDialog(it)?.let { d ->
                        dialog?.add(d) ?: run {
                            dialog = ArrayList<Dialog>().apply {
                                add(d)
                            }
                        }
                    } ?: error("not found error: the dialog named '$it' doesn't exist.")
                }
            }
        }
        section.findConfig("Action","action","actions","Actions")?.let { g ->
            g.getKeys(false).forEach {
                g.getAsStringList(it)?.let { s ->
                    ActionBuilder.create(adder,s)?.let { action ->
                        try {
                            addTalkTask(it.toInt()) { current ->
                                action.invoke(current.player,current.event)
                            }
                        } catch (ex: Exception) {
                            error("syntax error: the parameter \"$it\" is not an int.")
                        }
                    } ?: error("unable to get this action: $it")
                } ?: error("syntax error: the value \"$it\" is not a string.")
            }
        }
        section.findStringList("EndAction","end-action")?.let {
            ActionBuilder.create(adder,it)?.let { action ->
                addLastAction { current ->
                    action.invoke(current.player,current.event)
                }
            } ?: error("unable to get end action.")
        }
        section.findConfig("Gesture","gesture","gestures","Gestures")?.let { g ->
            g.getKeys(false).forEach {
                g.getString(it)?.let { s ->
                    try {
                        addTalkTask(it.toInt()) { current ->
                            GestureManager.play(current.player, s, current.npc.npc)
                        }
                    } catch (ex: Exception) {
                        error("syntax error: the parameter \"$it\" is not an int.")
                    }
                } ?: error("syntax error: the value \"$it\" is not a string.")
            }
        }
        section.findConfig("Sound","Sounds","sounds","sound")?.let { sound ->
            for (j in sound.getKeys(false)) {
                val data = sound.getString(j)?.let { t ->
                    SoundData.fromString(t)
                } ?: continue
                try {
                    addTalkTask(j.toInt()) { current ->
                        data.play(current.player)
                    }
                } catch (ex: Exception) {
                    error("syntax error: the parameter \"$j\" is not an int.")
                }
            }
        }
        section.findConfig("Interface","interface")?.let { c ->
            c.getKeys(false).forEach {
                c.getString(it)?.let { s ->
                    typingManagerMap[s]?.let { manager ->
                        try {
                            executorMap[it.toInt()] = manager
                        } catch (ex: Exception) {
                            error("syntax error: the argument \"$it\" is not an int.")
                        }
                    } ?: error("not found error: the interface named $s doesn't exist.")
                } ?: error("syntax error: the value \"$it\" is not a string.")
            }
        }
        section.findStringList("SubDialog","sub-dialog","LinkedSubDialog","linked-sub-dialog")?.let { t ->
            adder.addLazyTask {
                t.forEach {
                    DialogManager.getDialog(it)?.let { d ->
                        subDialog?.add(d) ?: run {
                            subDialog = ArrayList<Dialog>().apply {
                                add(d)
                            }
                        }
                    } ?: error("not found error: the dialog named '$it' doesn't exist.")
                }
            }
        }
        fun throwRuntimeError() = error("runtime error: unable to load the function.")
        section.findStringList("index","Index","Indexes","indexes")?.forEach {
            val matcher = ANNOTATION_PATTERN.matcher(it)
            while (matcher.find()) {
                val name = matcher.group("name")
                val value = matcher.group("value")
                val func = FunctionBuilder.evaluate(matcher.replaceAll(""))
                if (!Number::class.java.isAssignableFrom(func.getReturnType())) {
                    error("type mismatch error: index must be a number.")
                    continue
                }
                when (name.lowercase()) {
                    "set" -> addLastAction { current ->
                        (func.apply(current.event) as? Number)?.let { get ->
                            QuestAdderBukkit.getPlayerData(current.player)?.run {
                                npcIndexes[value] = get.toInt()
                            }
                        } ?: throwRuntimeError()
                    }
                    "add" -> addLastAction { current ->
                        (func.apply(current.event) as? Number)?.let { get ->
                            QuestAdderBukkit.getPlayerData(current.player)?.run {
                                npcIndexes[value] = (npcIndexes[value] ?: 0) + get.toInt()
                            }
                        } ?: throwRuntimeError()
                    }
                    "subtract" -> addLastAction { current ->
                        (func.apply(current.event) as? Number)?.let { get ->
                            QuestAdderBukkit.getPlayerData(current.player)?.run {
                                npcIndexes[value] = (npcIndexes[value] ?: 0) - get.toInt()
                            }
                        } ?: throwRuntimeError()
                    }
                    "multiply" -> addLastAction { current ->
                        (func.apply(current.event) as? Number)?.let { get ->
                            QuestAdderBukkit.getPlayerData(current.player)?.run {
                                npcIndexes[value] = (npcIndexes[value] ?: 0) * get.toInt()
                            }
                        } ?: throwRuntimeError()
                    }
                    "divide" -> addLastAction { current ->
                        (func.apply(current.event) as? Number)?.let { get ->
                            QuestAdderBukkit.getPlayerData(current.player)?.run {
                                npcIndexes[value] = (npcIndexes[value] ?: 0) / get.toInt()
                            }
                        } ?: throwRuntimeError()
                    }
                }
            }
        }
        section.findStringList("set-vars","SetVars","Vars","vars","Variables","variables","variable","Variable")?.forEach {
            val matcher = ANNOTATION_PATTERN.matcher(it)
            while (matcher.find()) {
                val name = matcher.group("name")
                val value = matcher.group("value")
                if (name == "remove") {
                    addLastAction { current ->
                        QuestAdderBukkit.getPlayerData(current.player)?.remove(value)
                    }
                } else {
                    val replacedMatcher = matcher.replaceAll("")
                    val func = FunctionBuilder.evaluate(replacedMatcher)
                    if (func.getReturnType() == Null::class.java) {
                        error("null error: this function returns null: $replacedMatcher")
                        continue
                    }
                    fun checkNumber(): Boolean {
                        val ret = Number::class.java.isAssignableFrom(func.getReturnType())
                        if (!ret) error("type mismatch error: this operation requires a number.")
                        return ret
                    }
                    when (name.lowercase()) {
                        "set" -> addLastAction { current ->
                            func.apply(current.event)?.let { any ->
                                QuestAdderBukkit.getPlayerData(current.player)?.set(value,any)
                            } ?: throwRuntimeError()
                        }
                        "putifabsent" -> addLastAction { current ->
                            func.apply(current.event)?.let { any ->
                                QuestAdderBukkit.getPlayerData(current.player)?.putIfAbsent(value,any)
                            } ?: throwRuntimeError()
                        }
                        "add" -> {
                            if (!checkNumber()) continue
                            addLastAction { current ->
                                QuestAdderBukkit.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    (func.apply(current.event) as? Number)?.let { get ->
                                        data.set(value, (if (original is Number) original.toDouble() else 0.0) + get.toDouble())
                                    } ?: throwRuntimeError()
                                }
                            }
                        }
                        "subtract" -> {
                            if (!checkNumber()) continue
                            addLastAction { current ->
                                QuestAdderBukkit.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    (func.apply(current.event) as? Number)?.let { get ->
                                        data.set(
                                            value,
                                            (if (original is Number) original.toDouble() else 0.0) - get.toDouble()
                                        )
                                    } ?: throwRuntimeError()
                                }
                            }
                        }
                        "multiply" -> {
                            if (!checkNumber()) continue
                            addLastAction { current ->
                                QuestAdderBukkit.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    (func.apply(current.event) as? Number)?.let { get ->
                                        data.set(
                                            value,
                                            (if (original is Number) original.toDouble() else 0.0) * get.toDouble()
                                        )
                                    } ?: throwRuntimeError()
                                }
                            }
                        }
                        "divide" -> {
                            if (!checkNumber()) continue
                            addLastAction { current ->
                                QuestAdderBukkit.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    (func.apply(current.event) as? Number)?.let { get ->
                                        data.set(
                                            value,
                                            (if (original is Number) original.toDouble() else 0.0) / get.toDouble()
                                        )
                                    } ?: throwRuntimeError()
                                }
                            }
                        }
                    }
                }
            }
        }
        section.findStringList("conditions","Conditions","condition","Condition")?.let { cond ->
            adder.addLazyTask {
                fun throwRuntimeError(result: Any?) {
                    error("runtime error: the value \"$result\" is not a boolean.")
                }
                cond.forEach {
                    val matcher = ANNOTATION_PATTERN.matcher(it)
                    if (matcher.find()) {
                        val name = matcher.group("name")
                        val flag = matcher.group("value")
                        val func = FunctionBuilder.evaluate(matcher.replaceAll(""))
                        when (name) {
                            "cast" -> {
                                val dialogs = flag.split(',').mapNotNull { d ->
                                    DialogManager.getDialog(d)
                                }
                                if (dialogs.isNotEmpty()) {
                                    addPredicate { e ->
                                        val result = func.apply(e.event)
                                        if ((result is Boolean)) {
                                            if (result) {
                                                dialogs.random().start(e)
                                                false
                                            } else true
                                        } else {
                                            throwRuntimeError(result)
                                            false
                                        }
                                    }
                                } else error("not found error: unable to find the dialog named \"$flag\".")
                            }
                            else -> {
                                addPredicate { e ->
                                    val result = func.apply(e.event)
                                    if (result is Boolean) {
                                        result
                                    } else {
                                        throwRuntimeError(result)
                                        false
                                    }
                                }
                            }
                        }
                    } else {
                        val func = FunctionBuilder.evaluate(it)
                        addPredicate { e ->
                            val result = func.apply(e.event)
                            if (result is Boolean) {
                                result
                            } else {
                                throwRuntimeError(result)
                                false
                            }
                        }
                    }
                }
            }
        }
        section.findStringList("quest","Quest","SetQuest","set-quest")?.forEach {
            val split = it.split(' ')
            if (split.size > 1) {
                adder.addLazyTask {
                    val quest = DialogManager.getQuest(split[0]) ?: run {
                        error("not found error: the quest named \"${split[0]}\" doesn't exist.")
                        return@addLazyTask
                    }
                    when (split[1].lowercase()) {
                        "give" -> addLastAction { current ->
                            quest.give(current.player)
                        }
                        "remove" -> addLastAction { current ->
                            quest.remove(current.player)
                        }
                        "complete" -> addLastAction { current ->
                            quest.complete(current.player)
                        }
                        else -> error("not found error: the quest action \"${split[1]}\" doesn't exist.")
                    }
                }
            }
        }
        section.findStringList("Check","check","CheckQuest","check-quest")?.forEach {
            val split = it.split(' ')
            if (split.size > 1) {
                adder.addLazyTask {
                    val quest = DialogManager.getQuest(split[0]) ?: run {
                        error("not found error: the quest named \"${split[0]}\" doesn't exist.")
                        return@addLazyTask
                    }
                    when (split[1].lowercase()) {
                        "has" -> { current: DialogCurrent ->
                            quest.has(current.player)
                        }
                        "complete" -> { current: DialogCurrent ->
                            quest.isCompleted(current.player)
                        }
                        "ready" -> { current: DialogCurrent ->
                            quest.isReady(current.player)
                        }
                        "clear" -> { current: DialogCurrent ->
                            quest.isCleared(current.player)
                        }
                        "!has" -> { current: DialogCurrent ->
                            !quest.has(current.player)
                        }
                        "!complete" -> { current: DialogCurrent ->
                            !quest.isCompleted(current.player)
                        }
                        "!ready" -> { current: DialogCurrent ->
                            !quest.isReady(current.player)
                        }
                        "!clear" -> { current: DialogCurrent ->
                            !quest.isCleared(current.player)
                        }
                        else -> {
                            error("not found error: the quest predicate \"${split[1]}\" doesn't exist.")
                            null
                        }
                    }?.let { pre ->
                        if (split.size > 2) {
                            val dialog = DialogManager.getDialog(split[2])
                            if (dialog == null) error("not found error: the dialog named ${split[2]} doesn't exist.")
                            else addPredicate { current ->
                                if (pre(current)) {
                                    dialog.start(current)
                                    false
                                } else true
                            }
                        } else {
                            addPredicate(pre)
                        }
                    }
                }
            }
        }
        section.findStringList("QnA","QnAs","qna","qnas")?.forEach {
            adder.addLazyTask {
                DialogManager.getQnA(it)?.let { q ->
                    qna?.add(q) ?: run {
                        qna = ArrayList<QnA>().apply {
                            add(q)
                        }
                    }
                } ?: error("not found error: the qna named $it doesn't exist.")
            }
        }
    }

    fun start(player: Player, npc: ActualNPC) {
        val run = DialogRun(player,npc)
        if (run.current.event.isCancelled) return
        start(run)
    }
    private fun start(current: DialogCurrent) {
        start(DialogRun(current.apply {
            safeEnd = false
            npc.questNPC.soundData.let {
                typingSoundMap[npc.questNPC.name] = it
                typingSoundMap.putAll(typingSound)
            }
        }))
    }
    private fun start(run: DialogRun) {
        val uuid = run.current.player.uniqueId
        if (!predicate(run.current)) {
            subDialog?.let { l ->
                if (l.isNotEmpty() && !playerTask.containsKey(uuid)) l.random().start(run.current)
            }
            return
        }
        if (playerTask.containsKey(uuid)) return
        playerTask[uuid] = run.apply {
            start()
        }
    }

    override fun getKey(): String {
        return dialogKey
    }
}