package kor.toxicity.questadder.mechanic.dialog

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.QuestAdder
import kor.toxicity.questadder.api.event.DialogStartEvent
import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.GuiExecutor
import kor.toxicity.questadder.api.gui.IGuiHolder
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.api.mechanic.DialogSender
import kor.toxicity.questadder.api.mechanic.IActualNPC
import kor.toxicity.questadder.api.mechanic.IDialog
import kor.toxicity.questadder.api.mechanic.MechanicBlueprint
import kor.toxicity.questadder.api.util.SoundData
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.*
import kor.toxicity.questadder.mechanic.qna.QnA
import kor.toxicity.questadder.nms.VirtualArmorStand
import kor.toxicity.questadder.shop.implement.Shop
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.gui.SubExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

class Dialog(adder: QuestAdder, val file: File, private val dialogKey: String, section: ConfigurationSection): IDialog {

    private val bluePrint = DialogBlueprint(section)
    override fun getOriginalBlueprint(): MechanicBlueprint {
        return bluePrint.copy()
    }

    private interface TypingExecutor {
        fun initialize(talker: Component?)
        fun run(talk: Component)
        fun end()
    }
    private interface TypingManager {
        fun create(current: DialogCurrent, jsonObject: JsonObject): TypingExecutor
    }

    private class DialogCurrent(
        val player: Player,
        val sender: DialogSender,
        val event: DialogStartEvent,
        var run: DialogRun
    ) {
        var executor: TypingExecutor? = null
        val state = DialogState()

        val typingSpeedMap = HashMap<String, Long>()
        val typingSoundMap = HashMap<String, SoundData>()
        var inventory: IGuiHolder? = null
        var display: VirtualArmorStand? = null
        var safeEnd = false
    }
    private inner class DialogRun {

        val current: DialogCurrent
        constructor(player: Player, questNPC: DialogSender) {
            this.current = DialogCurrent(player, questNPC, DialogStartEvent(
                player,
                questNPC,
                this@Dialog
            ).apply {
                call()
            },this)
        }
        constructor(dialogCurrent: DialogCurrent) {
            this.current = dialogCurrent.also {
                it.run = this
            }
        }
        private var talkIndex = 0
        private var talkerComponent: Component? = null
        private var task: BukkitTask? = null
        private var iterator = ComponentReader.emptyIterator()

        private var started = false


        fun start() {
            cancel()
            if (talkIndex < talk.size) {
                this@Dialog.executorMap[talkIndex + 1]?.let { d ->
                    current.executor?.end()
                    try {
                        current.executor = d.second.create(current, d.first)
                    } catch (ex: Exception) {
                        QuestAdderBukkit.warn("runtime error: cannot read this flag: ${d.first}")
                        QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    }
                }
                if (current.executor == null) current.executor = defaultExecutor.create(current, JsonObject())
                started = true
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
            val endData = DialogEndData(
                current.player,
                current.event,
                current.inventory?.data?.gui?.guiName ?: Component.empty(),
                talkerComponent ?: current.sender.talkerName.asComponent().deepClear(),
                if (talk.isNotEmpty()) talk.last().createComponent(current.event) ?: Component.empty() else null
            )
            shop?.let {
                if (it.isNotEmpty()) {
                    it.random().open(current.player, current.sender)
                }
                return
            }
            qna?.let {
                if (it.isNotEmpty()) {
                    it.random().open(endData) { d ->
                        d.start(current)
                    }
                    return
                }
            }
            current.state.tasks.forEach {
                it()
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
            var sound = current.sender.soundData
            var speed = current.sender.typingSpeed
            talkerComponent?.let {
                val onlyText = it.onlyText()
                sound = current.typingSoundMap[onlyText] ?: QuestAdderBukkit.Config.defaultTypingSound
                speed = current.typingSpeedMap[onlyText] ?: QuestAdderBukkit.Config.defaultTypingSpeed
            }
            task = QuestAdderBukkit.taskTimer(speed,speed) {
                if (iterator.hasNext()) {
                    val next = iterator.nextLine()
                    if ((next as TextComponent).content() != "*") {
                        sound.play(current.player)
                        current.executor!!.run(next)
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
        private val typingManagerPattern = Pattern.compile("^(?<name>(([a-zA-Z])+))(?<argument>\\{[\\w|\\W]*})?$")
        private val playerTask = ConcurrentHashMap<UUID, DialogRun>()

        fun skip(player: Player) = playerTask[player.uniqueId]?.current?.run?.start() != null
        fun end(player: Player) = playerTask[player.uniqueId]?.current?.run?.end() != null

        private val defaultExecutor = object : TypingManager {
            override fun create(current: DialogCurrent, jsonObject: JsonObject): TypingExecutor {
                val jsonGuiName = try {
                    jsonObject.getAsJsonPrimitive("name")?.let {
                        ComponentReader<Player>(it.asString).createComponent(current.player)
                    }
                } catch (ex: Exception) {
                    null
                }
                val getInv = current.sender.gui ?: createInventory("talking with ${current.sender.talkerName}".asComponent(),5)
                val selectedInv = jsonGuiName?.let {
                    getInv.setName(it)
                } ?: getInv
                val inv = selectedInv.open(current.player, object :
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
                            MouseButton.RIGHT -> {
                                current.run.end()
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

                    private var comp = Component.empty()

                    override fun initialize(talker: Component?) {
                        comp = Component.empty()
                        item.itemMeta = meta?.apply {
                            QuestAdderBukkit.platform.setDisplay(this, (talker ?: current.sender.talkerName.asComponent().deepClear()).append(":".asComponent().deepClear()))
                        }
                        for (i in 0..44) {
                            inv.inventory.setItem(i,null)
                        }
                        selectedInv.innerItems.forEach {
                            inv.inventory.setItem(it.key,it.value)
                        }

                        inv.inventory.setItem(22, item)
                        current.player.updateInventory()
                    }

                    override fun run(talk: Component) {
                        comp = comp.append(talk)
                        item.itemMeta = meta?.apply {
                            QuestAdderBukkit.platform.setLore(this, listOf(comp))
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

        private val typingManagerMap = HashMap<String, TypingManager>().apply {
            put("default", defaultExecutor)
            put("title", object : TypingManager {
                override fun create(current: DialogCurrent, jsonObject: JsonObject): TypingExecutor {
                    return object : TypingExecutor {
                        private var talker: Component = Component.empty()
                        private var comp = Component.empty()

                        override fun run(talk: Component) {
                            comp = comp.append(talk)
                            QuestAdderBukkit.audience.player(current.player).showTitle(Title.title(
                                talker,
                                comp,
                                Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofSeconds(2),
                                    Duration.ofSeconds(1)
                                )
                            ))
                        }
                        override fun initialize(talker: Component?) {
                            comp = Component.empty()
                            this.talker = talker ?: current.sender.talkerName.asComponent().deepClear()
                        }
                        override fun end() {
                        }
                    }
                }
            })
            put("text", object : TypingManager {
                override fun create(current: DialogCurrent, jsonObject: JsonObject): TypingExecutor {
                    return object : TypingExecutor {

                        private var referencedEntity = current.sender.entity ?: current.player
                        private var comp = Component.empty()

                        private val display = current.display ?: QuestAdderBukkit.nms.createArmorStand(current.player,referencedEntity.location).apply {
                            setText(Component.empty())
                            current.display = this
                        }
                        override fun end() {
                            display.remove()
                            current.display = null
                        }

                        override fun initialize(talker: Component?) {
                            comp = Component.empty()
                            referencedEntity = if (talker != null && talker.onlyText() == "player") {
                                current.player
                            }
                            else current.sender.entity ?: current.player
                        }

                        override fun run(talk: Component) {
                            comp = comp.append(talk)
                            display.setText(comp)
                            display.teleport(referencedEntity.location.apply {
                                y += 0.25
                            })
                        }
                    }
                }
            })
            put("tooltip", object : TypingManager {
                override fun create(current: DialogCurrent, jsonObject: JsonObject): TypingExecutor {

                    val name = jsonObject.getAsJsonPrimitive("name")?.asString ?: throw RuntimeException("\"name\" field not found.")
                    val data = ResourcePackManager.getToolTip(name) ?: throw RuntimeException("unable to find \"$name\" tooltip.")
                    val split = data.split

                    var gui = Component.empty()
                    if (data.fade) {
                        val onePixelBack = (-1).parseToSpaceComponent()
                        val halfPixelBack = (-512).parseToSpaceComponent()
                        gui = gui.append(halfPixelBack)
                            //Upper
                            .append(FADE_COMPONENT_UPPER)
                            .append(onePixelBack)
                            .append(FADE_COMPONENT_UPPER)
                            .append(onePixelBack)
                            .append(FADE_COMPONENT_UPPER)
                            .append(onePixelBack)
                            .append(FADE_COMPONENT_UPPER)

                            //1024 - 3
                            .append((-1021).parseToSpaceComponent())

                            //under
                            .append(FADE_COMPONENT_UNDER)
                            .append(onePixelBack)
                            .append(FADE_COMPONENT_UNDER)
                            .append(onePixelBack)
                            .append(FADE_COMPONENT_UNDER)
                            .append(onePixelBack)
                            .append(FADE_COMPONENT_UNDER)
                            .append(halfPixelBack)
                    }
                    gui = gui.append(data.offset.parseToSpaceComponent()).append(data.gui.component).append((-data.gui.width / 2).parseToSpaceComponent()).append(data.chatOffset.parseToSpaceComponent())

                    return object : TypingExecutor {
                        private var talker: Component = Component.empty()
                        private val list = ArrayList<Component>()

                        override fun initialize(talker: Component?) {
                            list.clear()
                            this.talker = talker ?: current.sender.talkerName.asComponent().deepClear()
                        }

                        override fun run(talk: Component) {
                            val talkList = ArrayList<Component>()
                            list.add(talk)
                            var result = gui.append(data.talker.offset.parseToSpaceComponent()).append(data.talker.font.asComponent(talker)).append((-data.talker.font.widthComponent(talker)).parseToSpaceComponent()).append((-data.talker.offset).parseToSpaceComponent())
                            list.forEachIndexed { index, component ->
                                val selectedData = data.talk[(index / split).coerceAtMost(data.talk.lastIndex)]
                                talkList.add(selectedData.font.asComponent(component))
                                if ((index + 1) % split == 0) {
                                    var comp = Component.empty()
                                    talkList.forEach {
                                        comp = comp.append(it)
                                    }
                                    result = result.append(selectedData.offset.parseToSpaceComponent())
                                        .append(comp)
                                        .append(selectedData.font.widthComponent(-talkList.size))
                                        .append((-selectedData.offset).parseToSpaceComponent())
                                    talkList.clear()
                                }
                            }
                            if (talkList.isNotEmpty()) {
                                val selectedData = data.talk[(list.size / split).coerceAtMost(data.talk.lastIndex)]
                                var comp = Component.empty()
                                talkList.forEach {
                                    comp = comp.append(it)
                                }
                                result = result.append(selectedData.offset.parseToSpaceComponent())
                                    .append(comp)
                                    .append(selectedData.font.widthComponent(-talkList.size))
                                    .append((-selectedData.offset).parseToSpaceComponent())
                            }
                            QuestAdderBukkit.audience.player(current.player).showTitle(Title.title(
                                Component.empty(),
                                result,
                                Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofSeconds(2),
                                    Duration.ofSeconds(1)
                                )
                            ))
                        }

                        override fun end() {
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
    private var shop: MutableList<Shop>? = null
    private var subDialog: MutableList<Dialog>? = null
    private var executorMap = HashMap<Int, Pair<JsonObject,TypingManager>>()
    private var predicate: (DialogCurrent) -> Boolean = {
        true
    }
    private var initialTask: (DialogCurrent) -> Unit = {}
    private var lastAction: (DialogCurrent) -> Unit = {}
    private var talkTask = HashMap<Int,(DialogCurrent) -> Unit>()
    private val typingSound = HashMap<String,SoundData>().apply {
        bluePrint.typingSound?.let { config ->
            config.getKeys(false).forEach {
                config.getAsSoundData(it)?.let { sound ->
                    put(it,sound)
                } ?: QuestAdderBukkit.warn("syntax error: unable to find sound data: $it ($dialogKey in ${file.name})")
            }
        }
    }
    private val typingSpeed = HashMap<String,Long>().apply {
        bluePrint.typingSpeed?.let { config ->
            config.getKeys(false).forEach {
                put(it,config.getLong(it).coerceAtLeast(1).coerceAtMost(4))
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
    fun addTalkAction(index: Int, block: (Player,DialogSender) -> Unit) {
        val act = talkTask[index] ?: {}
        talkTask[index] = {
            act(it)
            block(it.player,it.sender)
        }
    }
    /**
     * Add the action be called when this dialog ends.
     *
     * @param block the function be called.
     * @since 1.0
     */
    fun addLastAction(block: (Player,DialogSender) -> Unit) {
        val act = lastAction
        lastAction = {
            act(it)
            block(it.player,it.sender)
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
    private fun addInitialTask(action: (DialogCurrent) -> Unit) {
        val before = initialTask
        initialTask = {
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
        bluePrint.talk?.let { t ->
            t.forEach {
                val matcher = talkPattern.matcher(it)
                if (matcher.find()) {
                    talk.add(ComponentReader(matcher.group("talk")))
                    val talkerGroup = matcher.group("talker")
                    if (talkerGroup != null) talker[talk.size] = ComponentReader(talkerGroup)
                }
            }
        }
        bluePrint.talker?.let { config ->
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
        bluePrint.dialog?.let { t ->
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
        bluePrint.action?.let { g ->
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
        bluePrint.endAction?.let {
            ActionBuilder.create(adder,it.toList())?.let { action ->
                addLastAction { current ->
                    action.invoke(current.player,current.event)
                }
            } ?: error("unable to get end action.")
        }
        bluePrint.gestures?.let { g ->
            g.getKeys(false).forEach {
                g.getString(it)?.let { s ->
                    try {
                        addTalkTask(it.toInt()) { current ->
                            val sender = current.sender
                            if (sender is IActualNPC) GestureManager.play(current.player, s, sender.toCitizensNPC())
                        }
                    } catch (ex: Exception) {
                        error("syntax error: the parameter \"$it\" is not an int.")
                    }
                } ?: error("syntax error: the value \"$it\" is not a string.")
            }
        }
        bluePrint.sounds?.let { sound ->
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
        bluePrint.interfaces?.let { c ->
            c.getKeys(false).forEach {
                c.getString(it)?.let { s ->
                    val matcher = typingManagerPattern.matcher(s)
                    if (matcher.find()) {
                        val name = matcher.group("name")
                        val args = matcher.group("argument")?.let { argString ->
                            try {
                                JsonParser.parseString(argString).asJsonObject
                            } catch (ex: Exception) {
                                error("unable to read this json: $argString")
                                JsonObject()
                            }
                        } ?: JsonObject()
                        typingManagerMap[name]?.let { manager ->
                            try {
                                executorMap[it.toInt()] = args to manager
                            } catch (ex: Exception) {
                                error("syntax error: the argument \"$it\" is not an int.")
                            }
                        } ?: error("not found error: the interface named $s doesn't exist.")
                    } else error("syntax error: cannot read this typing manager syntax: $s")
                } ?: error("syntax error: the value \"$it\" is not a string.")
            }
        }
        bluePrint.subDialog?.let { t ->
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
        bluePrint.index?.forEach {
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
        bluePrint.variable?.forEach {
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
        bluePrint.checkQuest?.forEach {
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
        bluePrint.condition?.let { cond ->
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
        bluePrint.setQuest?.forEach {
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
                        "request" -> addLastAction { current ->
                            quest.request(current.player)
                        }
                        else -> error("not found error: the quest action \"${split[1]}\" doesn't exist.")
                    }
                }
            }
        }
        bluePrint.qna?.forEach {
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
        bluePrint.shops?.forEach {
            adder.addLazyTask {
                DialogManager.getShop(it)?.let { q ->
                    shop?.add(q) ?: run {
                        shop = ArrayList<Shop>().apply {
                            add(q)
                        }
                    }
                } ?: error("not found error: the shop named $it doesn't exist.")
            }
        }
        bluePrint.takeItem?.forEach {
            ItemManager.getItemSupplier(it)?.let { supplier ->
                addInitialTask { current ->
                    current.state.addEndTask {
                        current.player.take(supplier.get())
                    }
                }
            }
        }
        bluePrint.giveItem?.forEach {
            ItemManager.getItemSupplier(it)?.let { supplier ->
                addInitialTask { current ->
                    current.state.addEndTask {
                        current.player.give(supplier.get())
                    }
                }
            }
        }
    }

    override fun start(player: Player, sender: DialogSender): DialogState? {
        val run = DialogRun(player,sender)
        if (run.current.event.isCancelled) return null
        return start(run)
    }
    private fun start(current: DialogCurrent): DialogState? {
        return start(DialogRun(current.apply {
            safeEnd = false
            sender.soundData.let {
                typingSoundMap[sender.talkerName] = it
                typingSoundMap.putAll(typingSound)
            }
            inventory?.let {
                player.openInventory(it.inventory)
            }
            typingSpeedMap[sender.talkerName] = sender.typingSpeed
            typingSpeedMap.putAll(typingSpeed)
        }))
    }
    private fun start(run: DialogRun): DialogState?  {
        try {
            val uuid = run.current.player.uniqueId
            if (!predicate(run.current)) {
                subDialog?.let { l ->
                    if (l.isNotEmpty() && !playerTask.containsKey(uuid)) l.random().start(run.current)
                }
                return null
            }
            if (playerTask.containsKey(uuid)) return null
            playerTask[uuid] = run.apply {
                initialTask(current)
                start()
            }
            return run.current.state
        } catch (ex: Exception) {
            QuestAdderBukkit.warn("runtime error: ${ex.message}")
        } catch (ex: StackOverflowError) {
            QuestAdderBukkit.warn("infinite loop error: dialog loops infinitely!")
        }
        return null
    }

    override fun getKey(): String {
        return dialogKey
    }

    fun getEditor(player: Player) {
        DialogEditor(player)
    }
    private class DialogTalkData(
        val talk: String,
        val talker: String?,
        val sound: String?,
        val typingInterface: String?
    )
    private inner class DialogEditor(val player: Player) {
        private val print = bluePrint.copy()

        init {
            open()
        }
        fun open() {
            val mainComponent = "Dialog: ${file.name}: $dialog".asComponent()
            createInventory(mainComponent, 6).open(player, object : GuiExecutor {

                private var currentPage = 0
                private var talkList: List<DialogTalkData> = emptyList()

                override fun initialize(data: GuiData) {
                    val inv = data.inventory
                    for (i in 0..<54) {
                        inv.setItem(i, null)
                    }
                    setupTalk()
                    val platform = QuestAdderBukkit.platform
                    talkList.forEachIndexed { index, talkData ->
                        inv.setItem(9 + index, ItemStack(Material.BOOK).apply {
                            itemMeta = itemMeta?.apply {
                                platform.setDisplay(this, "Talker: ".asComponent(YELLOW).clear().append((talkData.talker ?: "<npc>").asComponent(
                                    WHITE).clear()))
                                platform.setLore(this, listOf(
                                    "Talk: ".asComponent(YELLOW).clear().append(talkData.talk.asComponent(WHITE).clear()),
                                    "Sound: ".asComponent(YELLOW).clear().append((talkData.sound ?: "<none>").asComponent(WHITE).clear()),
                                    "Interface: ".asComponent(YELLOW).clear().append((talkData.typingInterface ?: "<none>").asComponent(WHITE).clear()),
                                    Component.empty(),
                                    "(Left click - edit this talk)".asComponent(GRAY).clear()
                                ))
                            }
                        })
                    }
                    val pageLore = "Current Page - ${currentPage + 1} / ${(print.talk?.size ?: 0) / 36 + 1}".asClearComponent()
                    inv.setItem(46, ItemStack(Material.STONE_BUTTON).apply {
                        itemMeta = itemMeta?.apply {
                            platform.setDisplay(this, "Before Page".asClearComponent())
                            platform.setLore(this, listOf(
                                Component.empty(),
                                pageLore,
                                Component.empty(),
                                "(Click - Go to before page)".asComponent(GRAY).clear()
                            ))
                        }
                    })
                    inv.setItem(49, ItemStack(Material.ENCHANTED_BOOK).apply {
                        itemMeta = itemMeta?.apply {
                            platform.setDisplay(this, "Add new talk".asClearComponent())
                            platform.setLore(this, listOf(
                                Component.empty(),
                                "(Click - Add new talk)".asComponent(GRAY).clear()
                            ))
                        }
                    })
                    inv.setItem(52, ItemStack(Material.STONE_BUTTON).apply {
                        itemMeta = itemMeta?.apply {
                            platform.setDisplay(this, "After Page".asClearComponent())
                            platform.setLore(this, listOf(
                                Component.empty(),
                                pageLore,
                                Component.empty(),
                                "(Click - Go to After page)".asComponent(GRAY).clear()
                            ))
                        }
                    })
                    inv.setItem(45, ItemStack(Material.REDSTONE_BLOCK).apply {
                        itemMeta = itemMeta?.apply {
                            platform.setDisplay(this, "Exit without save".asComponent(RED).clear())
                            platform.setLore(this, listOf(
                                Component.empty(),
                                "(Click - exit without save)".asComponent(GRAY).clear()
                            ))
                        }
                    })
                    inv.setItem(53, ItemStack(Material.BEACON).apply {
                        itemMeta = itemMeta?.apply {
                            platform.setDisplay(this, "Save and exit".asComponent(GREEN).clear())
                            platform.setLore(this, listOf(
                                Component.empty(),
                                "(Click - save and exit)".asComponent(GRAY).clear()
                            ))
                        }
                    })
                }

                private fun setupTalk() {
                    print.talk?.let {
                        val newList = ArrayList<DialogTalkData>()
                        for (i in currentPage..<currentPage + 36) {
                            if (it.lastIndex < i) break
                            val talker: String?
                            val talk: String

                            val matcher = talkPattern.matcher(it[i])
                            if (matcher.find()) {
                                talker = matcher.group("talker")
                                talk = matcher.group("talk")
                            } else {
                                talker = print.talker?.getString(i.toString())
                                talk = it[i]
                            }
                            newList.add(DialogTalkData(
                                talk,
                                talker,
                                print.sounds?.getString(i.toString()),
                                print.interfaces?.getString(i.toString())
                            ))
                        }
                        talkList = newList
                    }
                }

                override fun click(
                    data: GuiData,
                    clickedItem: ItemStack,
                    clickedSlot: Int,
                    isPlayerInventory: Boolean,
                    button: MouseButton
                ) {
                    if (isPlayerInventory) return
                    if (clickedItem.type == Material.AIR) return
                    if (clickedSlot in 9..44) {
                        val selectedSlot = clickedSlot - 9 + currentPage * 36
                        val selectedData = talkList[selectedSlot]

                        fun rebase(pos: Int, amount: Int) {
                            print.talker?.rebase(pos, amount)
                            print.sounds?.rebase(pos, amount)
                            print.interfaces?.rebase(pos, amount)
                        }

                        when (button) {
                            MouseButton.LEFT -> {
                                createInventory(mainComponent.append(": ${selectedSlot + 1}".asComponent()),3).open(player, object : SubExecutor(data) {
                                    override fun initialize(data: GuiData) {
                                    }

                                    override fun click(
                                        data: GuiData,
                                        clickedItem: ItemStack,
                                        clickedSlot: Int,
                                        isPlayerInventory: Boolean,
                                        button: MouseButton
                                    ) {
                                        if (isPlayerInventory) return
                                        if (clickedItem.type == Material.AIR) return
                                    }

                                })
                            }
                            MouseButton.RIGHT -> {
                                val plus = selectedSlot + 1
                                print.talk = print.talk?.addElement(plus, "a new talk") ?: Array(1) {
                                    "a new talk"
                                }
                                rebase(plus, 1)
                                initialize(data)
                            }
                            MouseButton.SHIFT_RIGHT -> {
                                print.talk = print.talk?.removeAt(selectedSlot)
                                rebase(selectedSlot, -1)
                                initialize(data)
                            }
                            else -> {}
                        }
                    } else {
                        when (clickedSlot) {
                            46 -> {
                                currentPage = (currentPage - 1).coerceAtLeast(0)
                                initialize(data)
                            }
                            52 -> {
                                currentPage = (currentPage + 1).coerceAtMost((print.talk?.size ?: 0) / 36)
                                initialize(data)
                            }
                            49 -> {
                                print.talk = (print.talk?.addElement("a new talk") ?: Array(1) {
                                    "a new talk"
                                })
                                initialize(data)
                            }
                        }
                    }
                }

                override fun end(data: GuiData) {
                }

            })
        }

        fun save() {
            try {
                YamlConfiguration().run {
                    load(file)
                    set(dialogKey, print.config)
                    save(file)
                }
                player.info("successfully saved.")
                player.info("command \"/qa reload\" to apply your change.")
            } catch (ex: Exception) {
                player.warn("unable to save the dialog: ($dialog in ${file.name})")
                player.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
            player.closeInventory()
        }
    }
}
