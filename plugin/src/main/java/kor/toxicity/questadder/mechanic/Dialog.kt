package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.DialogStartEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.manager.GestureManager
import kor.toxicity.questadder.manager.SlateManager
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.nms.VirtualTextDisplay
import kor.toxicity.questadder.util.*
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

class Dialog(adder: QuestAdder, file: File, key: String, section: ConfigurationSection) {
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
        var typingSpeed = QuestAdder.Config.defaultTypingSpeed
        var inventory: GuiWrapper.GuiHolder? = null
        var display: VirtualTextDisplay? = null
        var safeEnd = false
    }
    private inner class DialogRun {

        val current: DialogCurrent

        constructor(player: Player, questNPC: ActualNPC) {
            this.current = DialogCurrent(player, questNPC, DialogStartEvent(player, questNPC, this@Dialog).apply {
                callEvent()
            },this)
        }
        constructor(dialogCurrent: DialogCurrent) {
            this.current = dialogCurrent.also {
                it.run = this
            }
        }
        private var talkIndex = 0
        private var talkComponent = Component.empty()
        private var task: BukkitTask? = null
        private var iterator = ComponentReader.emptyIterator()

        private var started = false

        fun start() {
            cancel()
            if (talkIndex < talk.size) {
                this@Dialog.executorMap[talkIndex + 1]?.let { d ->
                    current.executor = d.create(current)
                }
                if (current.executor == null) current.executor = defaultExecutor.create(current)
                started = true
                talkTask[talkIndex + 1]?.let { g ->
                    g(current)
                }
                talkComponent = Component.empty()
                current.executor?.initialize(talker[talkIndex + 1]?.createComponent(current.event))
                iterator = talk[talkIndex].createIterator(current.event) ?: ComponentReader.errorIterator()
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
            playerTask.remove(current.player.uniqueId)
            lastAction(current)
            dialog?.let {
                if (it.isNotEmpty()) {
                    it.random().start(current)
                    return
                }
            }
            current.executor!!.end()
        }

        fun restart() {
            if (!started) return
            cancel()
            startTask()
        }

        private fun startTask() {
            task = QuestAdder.taskTimer(current.typingSpeed,current.typingSpeed) {
                if (iterator.hasNext()) {
                    val next = iterator.nextLine()
                    if ((next as TextComponent).content() != "*") {
                        talkComponent = talkComponent.append(next)
                        current.player.playSound(current.player.location,Sound.BLOCK_STONE_BUTTON_CLICK_ON,1.0F,0.7F)
                        current.executor!!.run(talkComponent)
                    }
                } else {
                    cancel()
                    started = false
                    task = QuestAdder.taskLater(20) {
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
                current.display?.let {
                    it.remove()
                    current.display = null
                }
                val inv = current.inventory?.apply {
                    current.player.openInventory(inventory)
                } ?: createInventory("talking with ${current.npc.questNPC.name}".asComponent(),5).open(current.player, object : GuiExecutor {
                    override fun onEnd(inventory: Inventory) {
                        if (!current.safeEnd) {
                            current.run.stop()
                            SlateManager.slateOff(current.player)
                        }
                    }

                    override fun initialize(inventory: Inventory) {

                    }

                    override fun onClick(
                        inventory: Inventory,
                        isPlayerInventory: Boolean,
                        clickedItem: ItemStack,
                        clickedSlot: Int,
                        action: MouseButton
                    ) {
                        if (clickedSlot == 22) {
                            when (action) {
                                MouseButton.LEFT -> {
                                    current.typingSpeed = (current.typingSpeed - 1).coerceAtLeast(1)
                                    current.run.restart()
                                }
                                MouseButton.SHIFT_LEFT -> {
                                    current.run.start()
                                }
                                MouseButton.RIGHT,MouseButton.SHIFT_RIGHT -> {
                                    current.typingSpeed = (current.typingSpeed + 1).coerceAtMost(4)
                                    current.run.restart()
                                }
                                else -> {}
                            }
                        }
                    }
                }).apply {
                    current.inventory = this
                }
                val item = ItemStack(Material.ENCHANTED_BOOK)
                val meta = item.itemMeta

                return object : TypingExecutor {
                    override fun initialize(talker: Component?) {
                        item.itemMeta = meta.apply {
                            displayName((talker ?: current.npc.questNPC.name.asComponent().deepClear()).append(":".asComponent().deepClear()))
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
                        current.safeEnd = true
                        QuestAdder.task {
                            current.player.closeInventory()
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
                    current.display?.let {
                        it.remove()
                        current.display = null
                    }
                    if (!current.safeEnd) {
                        current.safeEnd = true
                        QuestAdder.task {
                            current.player.closeInventory()
                            current.inventory = null
                            current.safeEnd = false
                        }
                    }
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
                    if (!current.safeEnd) {
                        current.safeEnd = true
                        QuestAdder.task {
                            current.player.closeInventory()
                            current.inventory = null
                            current.safeEnd = false
                        }
                    }
                    return object : TypingExecutor {

                        private var referencedEntity = current.npc.npc.entity
                        private val display = current.display ?: QuestAdder.nms.createTextDisplay(current.player,referencedEntity.location).apply {
                            current.display = this
                        }
                        override fun end() {
                            display.remove()
                        }

                        override fun initialize(talker: Component?) {
                            referencedEntity = if (talker != null) {
                                current.player
                            }
                            else current.npc.npc.entity
                        }

                        override fun run(talk: Component) {
                            display.setText(talk)
                            display.teleport(referencedEntity.location.apply {
                                y += 2.25
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
    private var subDialog: MutableList<Dialog>? = null
    private var executorMap = HashMap<Int,TypingManager>()
    private var predicate: (DialogCurrent) -> Boolean = {
        true
    }
    private var lastAction: (DialogCurrent) -> Unit = {}
    private var talkTask = HashMap<Int,(DialogCurrent) -> Unit>()

    init {
        fun addTalkTask(i: Int, action: (DialogCurrent) -> Unit) {
            val act = talkTask[i] ?: {}
            talkTask[i] = {
                act(it)
                action(it)
            }
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
                        QuestAdder.warn("format error: $it is not a number. (${key} in ${file.name})")
                    }
                } ?: QuestAdder.warn("syntax error: the key '$it' is not a string. (${key} in ${file.name})")
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
                    } ?: QuestAdder.warn("not found error: the dialog named '$it' doesn't exist. (${key} in ${file.name})")
                }
            }
        }
        section.findConfig("Action","action","actions","Actions")?.let { g ->
            g.getKeys(false).forEach {
                g.getAsStringList(it)?.let { s ->
                    ActionBuilder.create(adder,s)?.let { action ->
                        try {
                            addTalkTask(it.toInt()) { current ->
                                action.apply(current.player)
                            }
                        } catch (ex: Exception) {
                            QuestAdder.warn("syntax error: the parameter \"$it\" is not an int. (${key} in ${file.name})")
                        }
                    } ?: QuestAdder.warn("unable to get this action: $it (${key} in ${file.name})")
                } ?: QuestAdder.warn("syntax error: the value \"$it\" is not a string. (${key} in ${file.name})")
            }
        }
        section.findConfig("Gesture","gesture","gestures","Gestures")?.let { g ->
            g.getKeys(false).forEach {
                g.getString(it)?.let { s ->
                    try {
                        addTalkTask(it.toInt()) { current ->
                            GestureManager.play(current.player, s, current.npc.npc)
                        }
                    } catch (ex: Exception) {
                        QuestAdder.warn("syntax error: the parameter \"$it\" is not an int. (${key} in ${file.name})")
                    }
                } ?: QuestAdder.warn("syntax error: the value \"$it\" is not a string. (${key} in ${file.name})")
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
                    QuestAdder.warn("syntax error: the parameter \"$j\" is not an int. (${key} in ${file.name})")
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
                            QuestAdder.warn("syntax error: the argument \"$it\" is not an int. (${key} in ${file.name})")
                        }
                    } ?: QuestAdder.warn("not found error: the interface named $s doesn't exist. (${key} in ${file.name})")
                } ?: QuestAdder.warn("syntax error: the value \"$it\" is not a string. (${key} in ${file.name})")
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
                    } ?: QuestAdder.warn("not found error: the dialog named '$it' doesn't exist. (${key} in ${file.name})")
                }
            }
        }
        section.findStringList("set-vars","SetVars","Vars","vars","Variables","variables","variable","Variable")?.let { vars ->
            fun isNotNumber(result: Any?) {
                QuestAdder.warn("runtime error: the value \"$result\" is not a number! ($key in ${file.name})")
            }
            fun isNull() {
                QuestAdder.warn("runtime error: the value is null! ($key in ${file.name})")
            }
            vars.forEach {
                val matcher = ANNOTATION_PATTERN.matcher(it)
                while (matcher.find()) {
                    val name = matcher.group("name")
                    val value = matcher.group("value")
                    val action = lastAction
                    if (name == "remove") {
                        lastAction = { current ->
                            action(current)
                            QuestAdder.getPlayerData(current.player)?.remove(value)
                        }
                    } else {
                        val func = FunctionBuilder.evaluate(matcher.replaceAll(""))
                        when (name) {
                            "set" -> lastAction = { current ->
                                action(current)
                                val get = func.apply(current.event)
                                get?.let { any ->
                                    QuestAdder.getPlayerData(current.player)?.set(value,any)
                                } ?: isNull()
                            }
                            "putifabsent" -> lastAction = { current ->
                                action(current)
                                val get = func.apply(current.event)
                                get?.let { any ->
                                    QuestAdder.getPlayerData(current.player)?.putIfAbsent(value,any)
                                } ?: isNull()
                            }
                            "add" -> lastAction = { current ->
                                action(current)
                                QuestAdder.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    val get = func.apply(current.event)
                                    if (get is Number) data.set(value, (if (original is Number) original.toDouble() else 0.0) + get.toDouble())
                                    else {
                                        isNotNumber(get)
                                    }
                                }
                            }
                            "subtract" -> lastAction = { current ->
                                action(current)
                                QuestAdder.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    val get = func.apply(current.event)
                                    if (get is Number) data.set(
                                        value,
                                        (if (original is Number) original.toDouble() else 0.0) - get.toDouble()
                                    )
                                    else {
                                        isNotNumber(get)
                                    }
                                }
                            }
                            "multiply" -> lastAction = { current ->
                                action(current)
                                QuestAdder.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    val get = func.apply(current.event)
                                    if (get is Number) data.set(
                                        value,
                                        (if (original is Number) original.toDouble() else 0.0) * get.toDouble()
                                    )
                                    else {
                                        isNotNumber(get)
                                    }
                                }
                            }
                            "divide" -> lastAction = { current ->
                                action(current)
                                QuestAdder.getPlayerData(current.player)?.let { data ->
                                    val original = data.get(value)
                                    val get = func.apply(current.event)
                                    if (get is Number) data.set(
                                        value,
                                        (if (original is Number) original.toDouble() else 0.0) / get.toDouble()
                                    )
                                    else {
                                        isNotNumber(get)
                                    }
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
                    QuestAdder.warn("runtime error: the value \"$result\" is not a boolean! ($key in ${file.name})")
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
                                    val n = predicate
                                    predicate = { e ->
                                        val result = func.apply(e.event)
                                        n(e) && if ((result is Boolean)) {
                                            if (result) {
                                                dialogs.random().start(e)
                                                false
                                            } else true
                                        } else {
                                            throwRuntimeError(result)
                                            false
                                        }
                                    }
                                } else QuestAdder.warn("not found error: unable to find the dialog named \"$flag\". ($key in ${file.name})")
                            }
                            else -> {
                                val n = predicate
                                predicate = { e ->
                                    val result = func.apply(e.event)
                                    n(e) && if (result is Boolean) {
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
                        val n = predicate
                        predicate = { e ->
                            val result = func.apply(e.event)
                            n(e) && if (result is Boolean) {
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
    }

    fun start(player: Player, npc: ActualNPC) {
        val run = DialogRun(player,npc)
        if (run.current.event.isCancelled) return
        start(run)
    }
    private fun start(current: DialogCurrent) {
        start(DialogRun(current))
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
}