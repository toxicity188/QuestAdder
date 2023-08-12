package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.event.ButtonGuiOpenEvent
import kor.toxicity.questadder.event.PlayerParseEvent
import kor.toxicity.questadder.event.QuestSurrenderEvent
import kor.toxicity.questadder.event.QuestSurrenderFailEvent
import kor.toxicity.questadder.event.TalkStartEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.mechanic.QnA
import kor.toxicity.questadder.mechanic.Quest
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.mechanic.npc.QuestNPC
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.action.RegistrableAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.event.AbstractEvent
import kor.toxicity.questadder.util.gui.MouseButton
import kor.toxicity.questadder.util.gui.player.PlayerGuiButtonType
import kor.toxicity.questadder.util.gui.player.PlayerGuiData
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.CitizensReloadEvent
import net.citizensnpcs.api.event.NPCDespawnEvent
import net.citizensnpcs.api.event.NPCSpawnEvent
import net.citizensnpcs.api.npc.NPC
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*
import kotlin.collections.HashMap

object DialogManager: QuestAdderManager {

    private val dialogMap = HashMap<String,Dialog>()
    private val actionMap = HashMap<String,RegistrableAction>()
    private val questMap = HashMap<String,Quest>()
    private val qnaMap = HashMap<String,QnA>()

    private val questNpcMap = HashMap<String, QuestNPC>()
    private val actualNPCMap = HashMap<UUID, ActualNPC>()

    private val exampleMap = mapOf(
        "actions" to "example-action.yml",
        "dialogs" to "example-dialog.yml"
    )

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                Dialog.stop(player)
                actionMap.values.forEach {
                    it.cancel(player)
                }
            }
            @EventHandler
            fun death(e: PlayerDeathEvent) {
                Dialog.stop(e.player)
            }
            @EventHandler
            fun click(e: InventoryClickEvent) {
                val player = e.whoClicked
                if (player is Player && Dialog.isRunning(player)) e.isCancelled = true
            }
            @EventHandler
            fun preProcess(e: PlayerCommandPreprocessEvent) {
                if (Dialog.isRunning(e.player)) e.isCancelled = true
            }

            @EventHandler
            fun click(e: PlayerInteractAtEntityEvent) {
                val uuid = e.rightClicked.uniqueId
                val player = e.player
                actualNPCMap[uuid]?.let {
                    val questNpc = it.questNPC
                    QuestAdder.getPlayerData(player)?.let { data ->
                        if (questNpc.dialogs.isNotEmpty()) {
                            val dialog = questNpc.dialogs[(data.npcIndexes.getOrPut(it.questNPC.name) {
                                0
                            }).coerceAtLeast(0).coerceAtMost(questNpc.dialogs.lastIndex)]
                            if (!TalkStartEvent(player,dialog,it).apply {
                                callEvent()
                                }.isCancelled) dialog.start(e.player,it)
                        }
                    }
                }
            }
            @EventHandler
            fun spawn(e: NPCSpawnEvent) {
                registerNPC(e.npc)
            }
            @EventHandler
            fun deSpawn(e: NPCDespawnEvent) {
                actualNPCMap.remove(e.npc.entity.uniqueId)
            }
            @EventHandler
            fun reload(e: CitizensReloadEvent) {
                dialogReload(adder)
            }
        },adder)
        adder.command.addCommand("parse") {
            aliases = arrayOf("p")
            description = "parse result from given arguments."
            length = 1
            usage = "parse <text>"
            allowedSender = arrayOf(SenderType.PLAYER)
            executor = { sender, args ->
                val str = args.toMutableList().apply {
                    removeAt(0)
                }.joinToString(" ")
                ComponentReader<PlayerParseEvent>(str).createComponent(PlayerParseEvent(sender as Player).apply {
                    callEvent()
                })?.let { component ->
                    sender.info(component)
                } ?: sender.info("cannot parse this text argument.")
            }
        }
        adder.command.addCommandAPI("var", arrayOf("v","변수"),"variable-related command.", true, CommandAPI("qa v")
            .addCommand("set") {
                aliases = arrayOf("s","설정")
                description = "set the variable."
                usage = "set <player> <name> <value>"
                length = 3
                executor = { sender, args ->
                    Bukkit.getPlayer(args[1])?.let { player ->
                        FunctionBuilder.evaluate(args.toMutableList().apply {
                            removeAt(0)
                            removeAt(0)
                            removeAt(0)
                        }.joinToString(" ")).apply(PlayerParseEvent(player).apply {
                            callEvent()
                        })?.let {
                            QuestAdder.getPlayerData(player)?.set(args[2],it)
                            sender.send("the variable sets: ${args[2]} to $it")
                        } ?: sender.send("set failure!")
                    }
                }
            })
        adder.getCommand("quest")?.setExecutor { sender, _, _, _ ->
            if (sender !is Player) {
                sender.send("player only command.")
            } else {
                val playerData = QuestAdder.getPlayerData(sender)
                if (playerData != null) {
                    var index = 0
                    var typeIndex = 0
                    val originalQuestData = playerData.getQuestKey().mapNotNull {
                        questMap[it]
                    }.toMutableList()
                    val types = TreeSet<String>().apply {
                        for (originalQuestDatum in originalQuestData) {
                            addAll(originalQuestDatum.type)
                        }
                    }.toList()
                    val playerGuiData = PlayerGuiData(types,null)
                    var questData = originalQuestData
                    fun initialize(inventory: Inventory) {
                        val startIndex = QuestAdder.Config.playerGuiStartIndex * 9
                        val maxIndex = QuestAdder.Config.playerGuiMaxIndex * 9
                        for (i in startIndex until (startIndex + maxIndex)) {
                            inventory.setItem(i,null)
                        }
                        questData.subList(index, (index + 9).coerceAtMost(questData.size).coerceAtMost(maxIndex)).forEachIndexed { index, quest ->
                            inventory.setItem(startIndex + index, quest.getIcon(sender))
                        }
                    }
                    QuestAdder.Config.playerGui.create(sender).run {
                        exceptAction = { inv, i, mouseButton ->
                            val quest = questData[i - QuestAdder.Config.playerGuiStartIndex * 9 + index]
                            when (mouseButton) {
                                MouseButton.SHIFT_LEFT -> {
                                    if (quest.cancellable) {
                                        QuestSurrenderEvent(quest,sender).callEvent()
                                        quest.remove(sender)
                                        questData.remove(quest)
                                        initialize(inv)
                                    } else {
                                        QuestSurrenderFailEvent(quest,sender).callEvent()
                                    }
                                }
                                else -> {}
                            }
                        }
                        PlayerGuiButtonType.values().forEach {
                            val t = QuestAdder.Config.getPlayerGuiButton(it) ?: return@forEach
                            val action: (Inventory,MouseButton) -> Unit = when (it) {
                                PlayerGuiButtonType.PAGE_BEFORE -> {
                                    { inv, button ->
                                        when (button) {
                                            MouseButton.LEFT,MouseButton.SHIFT_LEFT -> {
                                                index = (index - 1).coerceAtLeast(0)
                                                initialize(inv)
                                            }
                                            MouseButton.RIGHT,MouseButton.SHIFT_RIGHT -> {
                                                index = 0
                                                initialize(inv)
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                PlayerGuiButtonType.PAGE_AFTER -> {
                                    { inv, button ->
                                        when (button) {
                                            MouseButton.LEFT,MouseButton.SHIFT_LEFT -> {
                                                index = (index + 1).coerceAtMost((questData.size - 9).coerceAtLeast(0))
                                                initialize(inv)
                                            }
                                            MouseButton.RIGHT,MouseButton.SHIFT_RIGHT -> {
                                                index = questData.size
                                                initialize(inv)
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                PlayerGuiButtonType.TYPE_SORT -> {
                                    { inv, button ->
                                        if (types.isNotEmpty()) {
                                            fun reload() {
                                                playerGuiData.selectedType = types[typeIndex]
                                                questData = originalQuestData.filter { q ->
                                                    q.type.contains(types[typeIndex])
                                                }.toMutableList()
                                                initialize(inv)
                                            }
                                            when (button) {
                                                MouseButton.LEFT -> {
                                                    if (++typeIndex == types.size) typeIndex = 0
                                                    reload()
                                                }

                                                MouseButton.RIGHT -> {
                                                    if (typeIndex-- == 0) typeIndex = types.lastIndex
                                                    reload()
                                                }

                                                else -> {
                                                    questData = originalQuestData
                                                    playerGuiData.selectedType = null
                                                    initialize(inv)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            t.slot.forEach { int ->
                                addButton(int, { d ->
                                    it.applyItem(playerGuiData,t.item.write(d))
                                }, action)
                            }
                        }
                        open(player, ButtonGuiOpenEvent(player).apply {
                            callEvent()
                        }).inventory.apply {
                            initialize(this)
                        }
                    }
                }
            }
            true
        }
    }
    private fun registerNPC(npc: NPC) {
        questNpcMap.values.firstOrNull {
            npc.id == it.id
        }?.let {
            actualNPCMap[npc.entity.uniqueId] = ActualNPC(npc,it)
        }
    }

    private val actionReader: (QuestAdder,File,String,ConfigurationSection) -> Unit = { adder, file, key, c ->
        ActionBuilder.build(adder,c)?.let { build ->
            actionMap[key] = build
        } ?: QuestAdder.warn("unable to load action. ($key in ${file.name})")
    }
    private val dialogReader: (QuestAdder,File,String,ConfigurationSection) -> Unit = { adder, file, key, c ->
        dialogMap[key] = Dialog(adder,file,key,c)
    }
    private val qnaReader: (QuestAdder,File,String,ConfigurationSection) -> Unit = { adder, file, key, c ->
        qnaMap[key] = QnA(adder,file,key,c)
    }
    private val questReader: (QuestAdder,File,String,ConfigurationSection) -> Unit = { adder, file, key, c ->
        try {
            questMap[key] = Quest(adder, file, key, c)
        } catch (ex: Exception) {
            QuestAdder.warn("unable to load quest. (${file.name})")
            QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
        }
    }
    private val npcReader: (QuestAdder,File,String,ConfigurationSection) -> Unit = { adder, file, key, c ->
        try {
            questNpcMap[key] = QuestNPC(adder, file, key, c)
        } catch (ex: Exception) {
            QuestAdder.warn("unable to load NPC. ($key in ${file.name})")
            QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
        }
    }
    override fun reload(adder: QuestAdder) {
        dialogReload(adder)
    }

    fun getDialog(name: String) = dialogMap[name]
    fun getAction(name: String) = actionMap[name]
    fun getQuest(name: String) = questMap[name]
    fun getQnA(name: String) = qnaMap[name]

    private fun dialogReload(adder: QuestAdder) {

        exampleMap.forEach {
            val file = File(File(adder.dataFolder, it.key).apply {
                mkdir()
            }, it.value)
            if (!file.exists()) adder.getResource(it.value)?.use { stream ->
                try {
                    stream.buffered().use { bis ->
                        file.outputStream().use { fos ->
                            fos.buffered().use { bos ->
                                bis.copyTo(bos)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    QuestAdder.warn("unable to load example file: ${it.value}")
                }
            }
        }

        AbstractEvent.unregisterAll()
        actionMap.values.forEach {
            it.unregister()
        }

        qnaMap.clear()
        actionMap.clear()
        questMap.clear()
        dialogMap.clear()
        questNpcMap.clear()
        actualNPCMap.clear()

        fun loadConfig(name: String, reader: (QuestAdder,File,String,ConfigurationSection) -> Unit) {
            adder.loadFolder(name) { file, config ->
                config.getKeys(false).forEach {
                    config.getConfigurationSection(it)?.let { c ->
                        c.findString("Class","class")?.let { clazz ->
                            when (clazz.lowercase()) {
                                "dialog" -> dialogReader(adder,file,it,c)
                                "quest" -> questReader(adder,file,it,c)
                                "action" -> actionReader(adder,file,it,c)
                                "npc" -> npcReader(adder,file,it,c)
                                "qna" -> qnaReader(adder,file,it,c)
                                else -> reader(adder,file,it,c)
                            }
                        } ?: reader(adder,file,it,c)
                    } ?: QuestAdder.warn("syntax error: the key '$it' is not a configuration section. (${file.name})")
                }
            }
        }

        loadConfig("actions", actionReader)
        loadConfig("dialogs", dialogReader)
        loadConfig("qnas", qnaReader)
        loadConfig("quests", questReader)
        loadConfig("npcs", npcReader)

        CitizensAPI.getNPCRegistry().forEach {
            registerNPC(it)
        }
        Bukkit.getConsoleSender().run {
            send("${actionMap.size} of actions has successfully loaded.")
            send("${dialogMap.size} of dialogs has successfully loaded.")
            send("${questMap.size} of quests has successfully loaded.")
            send("${qnaMap.size} of QnAs has successfully loaded.")
            send("${actionMap.size} of NPCs successfully loaded.")
        }
    }

    override fun end(adder: QuestAdder) {
    }
}