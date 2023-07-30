package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.DialogStartEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.GuiExecutor
import kor.toxicity.questadder.util.GuiWrapper
import kor.toxicity.questadder.util.MouseButton
import kor.toxicity.questadder.util.function.FunctionBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.collections.HashMap

class Dialog(adder: QuestAdder, file: File, key: String, section: ConfigurationSection) {
    private interface TypingExecutor {
        fun initialize(talker: Component)
        fun run(talk: Component)
        fun end()
    }
    private interface TypingManager {
        fun create(current: DialogCurrent): TypingExecutor
    }

    private class DialogCurrent(
        val player: Player,
        val npc: QuestNPC,
        val event: DialogStartEvent,
        var run: DialogRun
    ) {
        var typingSpeed = QuestAdder.Config.defaultTypingSpeed
        var inventory: GuiWrapper.GuiHolder? = null
    }
    private inner class DialogRun {

        val current: DialogCurrent
        private var executor: TypingExecutor? = null

        constructor(player: Player, questNPC: QuestNPC) {
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
                if (executor == null) executor = this@Dialog.executor.create(current)
                started = true
                talkComponent = Component.empty()
                executor?.initialize(talker[talkIndex + 1]?.createComponent(current.event) ?: current.npc.name.asComponent().deepClear())
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
            dialog?.let {
                if (it.isNotEmpty()) {
                    it.random().start(current)
                    return
                }
            }
            executor!!.end()
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
                        executor!!.run(talkComponent)
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
        private val annotationPattern = Pattern.compile("@((?<name>(\\w|_|-)+):(?<value>(\\w|[가-힣]|_|-)+))")
        private val playerTask = ConcurrentHashMap<UUID,DialogRun>()
        private val defaultExecutor = object : TypingManager {
            override fun create(current: DialogCurrent): TypingExecutor {
                var stopped = false
                val inv = current.inventory ?: createInventory("talking with ${current.npc.name}".asComponent(),5).open(current.player, object : GuiExecutor {
                    override fun onEnd(inventory: Inventory) {
                        if (!stopped) current.run.stop()
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
                            println("Hello world!")
                        }
                    }
                }).apply {
                    current.inventory = this
                }
                val item = ItemStack(Material.ENCHANTED_BOOK)
                val meta = item.itemMeta

                return object : TypingExecutor {
                    override fun initialize(talker: Component) {
                        item.itemMeta = meta.apply {
                            displayName(talker.append(":".asComponent().deepClear()))
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
                        stopped = true
                        QuestAdder.task {
                            current.player.closeInventory()
                        }
                    }
                }
            }
        }
        private val executorMap = HashMap<String,TypingManager>().apply {
            put("default", defaultExecutor)
        }
    }
    private val talk = ArrayList<ComponentReader<DialogStartEvent>>()
    private val talker = HashMap<Int,ComponentReader<DialogStartEvent>>()
    private var dialog: MutableList<Dialog>? = null
    private var subDialog: MutableList<Dialog>? = null
    private var executor = defaultExecutor
    private var predicate: (DialogCurrent) -> Boolean = {
        true
    }

    init {
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
        section.findStringList("conditions","Conditions","condition","Condition")?.let { cond ->
            adder.addLazyTask {
                fun throwRuntimeError(result: Any?) {
                    QuestAdder.warn("runtime error: the value \"$result\" is not a boolean! ($key in ${file.name})")
                }
                cond.forEach {
                    val matcher = annotationPattern.matcher(it)
                    if (matcher.find()) {
                        val name = matcher.group("name")
                        val flag = matcher.group("value")
                        val func = FunctionBuilder.evaluate(matcher.replaceAll(""))
                        when (name) {
                            "cast" -> {
                                DialogManager.getDialog(flag)?.let { dialog ->
                                    val n = predicate
                                    predicate = { e ->
                                        val result = func.apply(e.event)
                                        n(e) && if ((result is Boolean)) {
                                            if (result) {
                                                dialog.start(e)
                                                false
                                            } else true
                                        } else {
                                            throwRuntimeError(result)
                                            false
                                        }
                                    }
                                } ?: QuestAdder.warn("not found error: unable to find the dialog named \"$flag\". ($key in ${file.name})")
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

    fun start(player: Player, npc: QuestNPC) {
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