package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.event.*
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.mechanic.QnA
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.mechanic.npc.QuestNPC
import kor.toxicity.questadder.mechanic.quest.Quest
import kor.toxicity.questadder.mechanic.quest.QuestRecord
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.action.RegistrableAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.event.AbstractEvent
import kor.toxicity.questadder.util.gui.Gui
import kor.toxicity.questadder.util.gui.GuiData
import kor.toxicity.questadder.util.gui.MouseButton
import kor.toxicity.questadder.util.gui.SubExecutor
import kor.toxicity.questadder.util.gui.player.PlayerGuiButtonType
import kor.toxicity.questadder.util.gui.player.PlayerGuiData
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.CitizensReloadEvent
import net.citizensnpcs.api.event.NPCDespawnEvent
import net.citizensnpcs.api.event.NPCSpawnEvent
import net.citizensnpcs.api.npc.NPC
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

object DialogManager: QuestAdderManager {

    private val dialogMap = HashMap<String,Dialog>()
    private val actionMap = HashMap<String,RegistrableAction>()
    private val questMap = HashMap<String, Quest>()
    private val qnaMap = HashMap<String,QnA>()

    private val questNpcMap = HashMap<String, QuestNPC>()
    private val actualNPCMap = HashMap<UUID, ActualNPC>()

    private val exampleMap = mapOf(
        "actions" to "example-action.yml",
        "dialogs" to "example-dialog.yml"
    )

    private val selectedQuestMap = ConcurrentHashMap<UUID, Quest>()

    fun getSelectedQuest(player: Player) = selectedQuestMap[player.uniqueId]

    override fun start(adder: QuestAdder) {

        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                Dialog.stop(player)
                actionMap.values.forEach {
                    it.cancel(player)
                }
                selectedQuestMap.remove(player.uniqueId)
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
            fun drop(e: PlayerDropItemEvent) {
                if (Dialog.isRunning(e.player)) e.isCancelled = true
            }
            @EventHandler
            fun complete(e: QuestCompleteEvent) {
                val uuid = e.player.uniqueId
                if (selectedQuestMap[uuid] == e.quest) selectedQuestMap.remove(uuid)
            }
            @EventHandler
            fun remove(e: QuestRemoveEvent) {
                val uuid = e.player.uniqueId
                if (selectedQuestMap[uuid] == e.quest) selectedQuestMap.remove(uuid)
            }
            @EventHandler
            fun click(e: PlayerInteractAtEntityEvent) {
                val uuid = e.rightClicked.uniqueId
                val player = e.player
                actualNPCMap[uuid]?.let {
                    val questNpc = it.questNPC
                    QuestAdder.getPlayerData(player)?.let { data ->
                        if (questNpc.dialogs.isNotEmpty()) {
                            val dialog = questNpc.dialogs[(data.npcIndexes.getOrPut(it.questNPC.key) {
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
                actualNPCMap.remove(e.npc.entity.uniqueId)?.cancel()
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
                    val originalQuestData = playerData.questVariables.mapNotNull {
                        if (it.value.state == QuestRecord.HAS) questMap[it.key] else null
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
                        questData.subList(index, (index + 9 * maxIndex).coerceAtMost(questData.size)).forEachIndexed { index, quest ->
                            inventory.setItem(startIndex + index, quest.getIcon(sender))
                        }
                        sender.updateInventory()
                    }
                    QuestAdder.Config.playerGui.create(sender).run {
                        initializer = {
                            initialize(it)
                        }
                        exceptAction = { inv, _, i, mouseButton ->
                            val get = i - QuestAdder.Config.playerGuiStartIndex * 9 + index
                            if (get >= 0 && get < questData.size) {
                                val quest = questData[get]
                                when (mouseButton) {
                                    MouseButton.LEFT -> {
                                        if (!NavigationManager.onNavigate(player)) {
                                            quest.locationList?.let { loc ->
                                                Gui(6, QuestAdder.Config.navigationGuiName, HashMap<Int, ItemStack>().apply {
                                                    loc.forEachIndexed { index, namedLocation ->
                                                        set(9 + index, ItemStack(namedLocation.material).apply {
                                                            itemMeta = itemMeta?.apply {
                                                                setCustomModelData(namedLocation.customModelData)
                                                                displayName(namedLocation.name)
                                                                val l = namedLocation.location
                                                                lore(listOf(
                                                                    Component.empty(),
                                                                    QuestAdder.Prefix.info.append("x: ${l.x.withComma()}, y: ${l.y.withComma()}, z: ${l.z.withComma()}".asClearComponent().color(
                                                                        WHITE))
                                                                ))
                                                            }
                                                        })
                                                    }
                                                }).open(player, object : SubExecutor(inv) {
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
                                                        val t = clickedSlot - 9
                                                        if (t < 0 || t > loc.lastIndex) return
                                                        NavigateStartEvent(player,loc[t]).callEvent()
                                                        NavigationManager.startNavigate(player,loc[t])
                                                        safeEnd = true
                                                        player.closeInventory()
                                                    }
                                                })
                                            } ?: NavigateFailEvent(player).callEvent()
                                        } else {
                                            NavigationManager.endNavigate(player)
                                            NavigateEndEvent(player).callEvent()
                                            player.closeInventory()
                                        }
                                    }
                                    MouseButton.RIGHT -> {
                                        if (selectedQuestMap.remove(player.uniqueId) == null) {
                                            selectedQuestMap[player.uniqueId] = quest
                                            QuestSelectEvent(quest, player).callEvent()
                                        }
                                    }
                                    MouseButton.SHIFT_LEFT -> {
                                        if (quest.cancellable) {
                                            QuestSurrenderEvent(quest, sender).callEvent()
                                            quest.remove(sender)
                                            questData.remove(quest)
                                            initialize(inv.inventory)
                                        } else {
                                            QuestSurrenderFailEvent(quest, sender).callEvent()
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        }
                        PlayerGuiButtonType.values().forEach {
                            val t = QuestAdder.Config.getPlayerGuiButton(it) ?: return@forEach
                            val action: (GuiData,MouseButton) -> Unit = when (it) {
                                PlayerGuiButtonType.PAGE_BEFORE -> {
                                    { i, button ->
                                        when (button) {
                                            MouseButton.LEFT,MouseButton.SHIFT_LEFT -> {
                                                index = (index - 1).coerceAtLeast(0)
                                                initialize(i.inventory)
                                            }
                                            MouseButton.RIGHT,MouseButton.SHIFT_RIGHT -> {
                                                index = 0
                                                initialize(i.inventory)
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                PlayerGuiButtonType.PAGE_AFTER -> {
                                    { i, button ->
                                        when (button) {
                                            MouseButton.LEFT,MouseButton.SHIFT_LEFT -> {
                                                index = (index + 1).coerceAtMost((questData.size - 9).coerceAtLeast(0))
                                                initialize(i.inventory)
                                            }
                                            MouseButton.RIGHT,MouseButton.SHIFT_RIGHT -> {
                                                index = questData.size
                                                initialize(i.inventory)
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                                PlayerGuiButtonType.TYPE_SORT -> {
                                    { i, button ->
                                        if (types.isNotEmpty()) {
                                            fun reload() {
                                                playerGuiData.selectedType = types[typeIndex]
                                                questData = originalQuestData.filter { q ->
                                                    q.type.contains(types[typeIndex])
                                                }.toMutableList()
                                                initialize(i.inventory)
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
                                                    initialize(i.inventory)
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
                        })
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


    override fun reload(adder: QuestAdder) {
        dialogReload(adder)
    }

    fun getDialog(name: String) = dialogMap[name]
    fun getAction(name: String) = actionMap[name]
    fun getQuest(name: String) = questMap[name]
    fun getQnA(name: String) = qnaMap[name]
    fun getNPC(uuid: UUID) = actualNPCMap[uuid]

    fun getDialogKeys() = dialogMap.keys.toList()
    fun getActionKeys() = actionMap.keys.toList()
    fun getQuestKeys() = questMap.keys.toList()
    fun getQnAKeys() = qnaMap.keys.toList()
    fun getNPCKeys() = actualNPCMap.keys.toList()
    fun getQuestNPCKeys() = questNpcMap.keys.toList()

    fun getNPC(name: String) = actualNPCMap.values.firstOrNull {
        it.questNPC.key == name
    }
    fun getQuestNPC(name: String) = questNpcMap[name]
    fun getAllNPC(): Set<ActualNPC> = HashSet(actualNPCMap.values)

    private fun dialogReload(adder: QuestAdder) {
        val actionReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            ActionBuilder.build(adder,c)?.let { build ->
                actionMap[key] = build
            } ?: QuestAdder.warn("unable to load action. ($key in ${file.name})")
        }
        val dialogReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                dialogMap[key] = Dialog(adder,file,key,c)
            } catch (ex: Exception) {
                QuestAdder.warn("unable to load dialog. (${file.name})")
                QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
        val qnaReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            qnaMap[key] = QnA(adder,file,key,c)
        }
        val questReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                questMap[key] = Quest(adder, file, key, c)
            } catch (ex: Exception) {
                QuestAdder.warn("unable to load quest. (${file.name})")
                QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
        val npcReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                questNpcMap[key] = QuestNPC(adder, file, key, c)
            } catch (ex: Exception) {
                QuestAdder.warn("unable to load NPC. ($key in ${file.name})")
                QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }

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
        actualNPCMap.values.forEach {
            it.cancel()
        }
        actionMap.values.forEach {
            it.unregister()
        }

        qnaMap.clear()
        actionMap.clear()
        questMap.clear()
        dialogMap.clear()
        questNpcMap.clear()
        actualNPCMap.clear()

        fun loadConfig(name: String, reader: (File,String,ConfigurationSection) -> Unit) {
            adder.loadFolder(name) { file, config ->
                config.getKeys(false).forEach {
                    config.getConfigurationSection(it)?.let { c ->
                        c.findString("Class","class")?.let { clazz ->
                            when (clazz.lowercase()) {
                                "dialog" -> dialogReader(file,it,c)
                                "quest" -> questReader(file,it,c)
                                "action" -> actionReader(file,it,c)
                                "npc" -> npcReader(file,it,c)
                                "qna" -> qnaReader(file,it,c)
                                else -> reader(file,it,c)
                            }
                        } ?: reader(file,it,c)
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
            send("${questNpcMap.size} of NPCs successfully loaded.")
        }
        QuestAdder.nms.updateCommand()
        val iterator = selectedQuestMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val quest = questMap[entry.value.key]
            if (quest == null) iterator.remove()
            else entry.setValue(quest)
        }
    }

    override fun end(adder: QuestAdder) {
    }
}