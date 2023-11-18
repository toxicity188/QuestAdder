package kor.toxicity.questadder.manager

import com.google.gson.JsonObject
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.*
import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.api.mechanic.DialogSender
import kor.toxicity.questadder.api.mechanic.QuestRecord
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.mechanic.dialog.Dialog
import kor.toxicity.questadder.mechanic.qna.QnA
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.mechanic.npc.QuestNPC
import kor.toxicity.questadder.mechanic.quest.Quest
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.api.mechanic.RegistrableAction
import kor.toxicity.questadder.mechanic.sender.DialogSenderType
import kor.toxicity.questadder.mechanic.sender.ItemDialogSender
import kor.toxicity.questadder.shop.blueprint.ShopBlueprint
import kor.toxicity.questadder.shop.implement.Shop
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.gui.Gui
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
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.collections.HashSet

object DialogManager: QuestAdderManager {

    private val dialogMap = HashMap<String, Dialog>()
    private val actionMap = HashMap<String, RegistrableAction>()
    private val questMap = HashMap<String, Quest>()
    private val qnaMap = HashMap<String, QnA>()
    private val senderMap = HashMap<String, DialogSender>()
    private val shopMap = ConcurrentHashMap<String, Shop>()

    private val questNpcMap = HashMap<String, QuestNPC>()
    private val npcIdMap = HashMap<Int, QuestNPC>()
    private val actualNPCMap = ConcurrentHashMap<UUID, ActualNPC>()

    private val exampleMap = mapOf(
        "actions" to "example-action.yml",
        "dialogs" to "example-dialog.yml"
    )

    private val selectedQuestMap = ConcurrentHashMap<UUID, Quest>()

    fun getSelectedQuest(player: Player) = selectedQuestMap[player.uniqueId]

    override fun start(adder: QuestAdderBukkit) {
        val pluginManager = Bukkit.getPluginManager()
        pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                Dialog.stop(player)
                ActionBuilder.cancelAll(player, ActionCancelEvent.CancelReason.QUIT)
                selectedQuestMap.remove(player.uniqueId)
            }
            @EventHandler
            fun death(e: PlayerDeathEvent) {
                val player = e.entity
                Dialog.stop(player)
                ActionBuilder.cancelAll(player, ActionCancelEvent.CancelReason.DEATH)
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
                val player = e.player

                val npc = CitizensAPI.getNPCRegistry().getNPC(e.rightClicked) ?: return

                actualNPCMap[npc.uniqueId]?.let {

                    val questNpc = it.questNPC
                    QuestAdderBukkit.getPlayerData(player)?.let { data ->

                        if (questNpc.dialogs.isNotEmpty()) {
                            val dialog = questNpc.dialogs[(data.npcIndexes.getOrPut(it.questNPC.npcKey) {
                                0
                            }).coerceAtLeast(0).coerceAtMost(questNpc.dialogs.lastIndex)]
                            if (TalkStartEvent(player, dialog, it).call()) dialog.start(e.player,it)
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
                actualNPCMap.remove(e.npc.entity.uniqueId)?.let {
                    senderMap.remove(it.questNPC.npcKey)
                    it.cancel()
                }
            }
            @EventHandler
            fun reload(e: CitizensReloadEvent) {
                QuestAdderBukkit.reloadSync()
            }

            @EventHandler
            fun itemClick(e: PlayerInteractEvent) {
                e.item?.let {
                    when (e.action) {
                        Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
                            it.itemMeta?.persistentDataContainer?.get(QUEST_ADDER_SENDER_KEY, PersistentDataType.STRING)?.let { key ->
                                senderMap[key]?.let { sender ->
                                    if (sender is ItemDialogSender) {
                                        sender.start(e.player)?.let { state ->
                                            if (sender.consume) state.addEndTask {
                                                it.amount -= 1
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
            @EventHandler
            fun swap(e: PlayerSwapHandItemsEvent) {
                val player = e.player
                if (!player.isSneaking) e.isCancelled = Dialog.skip(player)
                else e.isCancelled = Dialog.end(player)
            }
        },adder)
        adder.command.addCommandAPI("sender", arrayOf("s"), "sender-related command.", true, CommandAPI("qa s")
            .addCommand("give") {
                aliases = arrayOf("g")
                description = "give item sender to player."
                length = 2
                usage = "give <player> <sender>"
                executor = { sender, args ->
                    Bukkit.getPlayer(args[1])?.let {
                        senderMap[args[2]]?.let { s ->
                            if (s is ItemDialogSender) {
                                s.give(it)
                            } else sender.warn("this sender is not item sender.")
                        } ?: sender.warn("sender not found.")
                    } ?: sender.warn("the player \"${args[1]}\" is not online.")
                }
                tabComplete = { _, args ->
                    if (args.size == 3) senderMap.entries.filter {
                        it.value is ItemDialogSender && it.key.startsWith(args[2])
                    }.map {
                        it.key
                    } else null
                }
            }
            .addCommand("run") {
                aliases = arrayOf("r")
                description = "run dialog to represented sender."
                length = 3
                usage = "run <player> <dialog> <sender>"
                executor = { sender, args ->
                    Bukkit.getPlayer(args[1])?.let {
                        dialogMap[args[2]]?.let { d ->
                            senderMap[args[3]]?.let { s ->
                                d.start(it,s)
                            } ?: sender.warn("the sender \"${args[3]}\" not found.")
                        } ?: sender.warn("the dialog \"${args[2]}\" not found.")
                    } ?: sender.warn("the player \"${args[1]}\" is not online.")
                }
                tabComplete = { _, args ->
                    when (args.size) {
                        3 -> dialogMap.keys.filter {
                            it.startsWith(args[2])
                        }
                        4 -> senderMap.keys.filter {
                            it.startsWith(args[3])
                        }
                        else -> null
                    }
                }
            }
        )
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
                ComponentReader<PlayerParseEvent>(str).createComponent(
                    PlayerParseEvent(sender as Player).apply {
                    call()
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
                        }.joinToString(" ")).apply(
                            PlayerParseEvent(
                                player
                            ).apply {
                            call()
                        })?.let {
                            QuestAdderBukkit.getPlayerData(player)?.set(args[2],it)
                            sender.send("the variable sets: ${args[2]} to $it")
                        } ?: sender.send("set failure!")
                    }
                }
            })
        adder.getCommand("quest")?.setExecutor { sender, _, _, _ ->
            if (sender !is Player) {
                sender.send("player only command.")
            } else {
                val playerData = QuestAdderBukkit.getPlayerData(sender)
                if (playerData != null) {
                    var index = 0
                    var typeIndex = 0
                    val originalQuestData = playerData.questVariables.mapNotNull {
                        if (it.value.state == QuestRecord.HAS) questMap[it.key] else null
                    }.sorted().toMutableList()
                    val types = TreeSet<String>().apply {
                        for (originalQuestDatum in originalQuestData) {
                            addAll(originalQuestDatum.type)
                        }
                    }.toList()
                    val playerGuiData = PlayerGuiData(types,null)
                    var questData = originalQuestData
                    fun initialize(inventory: Inventory) {
                        val startIndex = QuestAdderBukkit.Config.playerGuiStartIndex * 9
                        val maxIndex = QuestAdderBukkit.Config.playerGuiMaxIndex * 9
                        for (i in startIndex until (startIndex + maxIndex)) {
                            inventory.setItem(i,null)
                        }
                        questData.subList(index, (index + 9 * maxIndex).coerceAtMost(questData.size)).forEachIndexed { index, quest ->
                            inventory.setItem(startIndex + index, quest.getIcon(sender, QuestAdderBukkit.Config.questSuffix))
                        }
                        sender.updateInventory()
                    }
                    QuestAdderBukkit.Config.playerGui.create(sender).run {
                        initializer = {
                            initialize(it)
                        }
                        exceptAction = { inv, _, i, mouseButton ->
                            val get = i - QuestAdderBukkit.Config.playerGuiStartIndex * 9 + index
                            if (get >= 0 && get < questData.size) {
                                val quest = questData[get]
                                when (mouseButton) {
                                    MouseButton.LEFT -> {
                                        if (!NavigationManager.onNavigate(player)) {
                                            quest.locationList?.let { loc ->
                                                Gui(6, QuestAdderBukkit.Config.navigationGuiName, HashMap<Int, ItemStack>().apply {
                                                    loc.forEachIndexed { index, namedLocation ->
                                                        set(9 + index, ItemStack(namedLocation.material).apply {
                                                            itemMeta = itemMeta?.apply {
                                                                setCustomModelData(namedLocation.customModelData)
                                                                QuestAdderBukkit.platform.setDisplay(this, namedLocation.name)
                                                                val l = namedLocation.location
                                                                QuestAdderBukkit.platform.setLore(this,listOf(
                                                                    Component.empty(),
                                                                    QuestAdderBukkit.Prefix.info.append("x: ${l.x.withComma()}, y: ${l.y.withComma()}, z: ${l.z.withComma()}".asClearComponent().color(
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
                                                        NavigateStartEvent(
                                                            player,
                                                            loc[t]
                                                        ).call()
                                                        NavigationManager.startNavigate(player,loc[t])
                                                        safeEnd = true
                                                        player.closeInventory()
                                                    }
                                                })
                                            } ?: NavigateFailEvent(
                                                player
                                            ).call()
                                        } else {
                                            NavigationManager.endNavigate(player)
                                            NavigateEndEvent(player)
                                                .call()
                                            player.closeInventory()
                                        }
                                    }
                                    MouseButton.RIGHT -> {
                                        if (selectedQuestMap.remove(player.uniqueId) == null) {
                                            selectedQuestMap[player.uniqueId] = quest
                                            QuestSelectEvent(
                                                quest,
                                                player
                                            ).call()
                                        }
                                    }
                                    MouseButton.SHIFT_LEFT -> {
                                        if (quest.cancellable) {
                                            QuestSurrenderEvent(
                                                quest,
                                                sender
                                            ).call()
                                            quest.remove(sender)
                                            questData.remove(quest)
                                            initialize(inv.inventory)
                                        } else {
                                            QuestSurrenderFailEvent(
                                                quest,
                                                sender
                                            ).call()
                                        }
                                    }

                                    else -> {}
                                }
                            }
                        }
                        PlayerGuiButtonType.entries.forEach {
                            val t = QuestAdderBukkit.Config.getPlayerGuiButton(it) ?: return@forEach
                            val action: (GuiData, MouseButton) -> Unit = when (it) {
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
                            call()
                        })
                    }
                }
            }
            true
        }
        QuestAdderBukkit.asyncTaskTimer(QuestAdderBukkit.Config.autoSaveTime, QuestAdderBukkit.Config.autoSaveTime) {
            shopMap.forEach {
                if (!QuestAdderBukkit.DB.using.saveShop(adder, it.value)) QuestAdderBukkit.warn("unable to save this shop: ${it.value.getKey()}")
            }
        }
    }
    private fun registerNPC(npc: NPC) {
        npcIdMap[npc.id]?.let {
            val uuid = npc.uniqueId
            if (!actualNPCMap.containsKey(uuid)) {
                val actual = ActualNPC(npc,it)
                actualNPCMap[uuid] = actual
                senderMap[actual.questNPC.npcKey] = actual
            }
        }
    }


    override fun reload(adder: QuestAdderBukkit) {
        dialogReload(adder)
    }

    fun getDialog(name: String) = dialogMap[name]
    fun getAction(name: String) = actionMap[name]
    fun getQuest(name: String) = questMap[name]
    fun getQnA(name: String) = qnaMap[name]
    fun getNPC(uuid: UUID) = actualNPCMap[uuid]
    fun getDialogSender(name: String) = senderMap[name]

    fun getDialogKeys() = dialogMap.keys.toList()
    fun getActionKeys() = actionMap.keys.toList()
    fun getQuestKeys() = questMap.keys.toList()
    fun getQnAKeys() = qnaMap.keys.toList()
    fun getNPCKeys() = actualNPCMap.values.map {
        it.questNPC.key
    }
    fun getQuestNPCKeys() = questNpcMap.keys.toList()

    fun getNPC(name: String) = actualNPCMap.values.firstOrNull {
        it.questNPC.npcKey == name
    }
    fun getShop(name: String) = shopMap[name]
    fun getShopKey() = shopMap.keys.toList()
    fun getQuestNPC(name: String) = questNpcMap[name]
    fun getAllNPC(): Set<ActualNPC> = HashSet(actualNPCMap.values)

    private fun dialogReload(adder: QuestAdderBukkit) {
        val actionReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            ActionBuilder.build(QuestAdderBukkit.Companion,c)?.let { build ->
                actionMap.put(key,build)?.let {
                    QuestAdderBukkit.warn("action name collision found: $key")
                }
                Unit
            } ?: QuestAdderBukkit.warn("unable to load action. ($key in ${file.name})")
        }
        val dialogReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                dialogMap.put(key,Dialog(QuestAdderBukkit,file,key,c))?.let {
                    QuestAdderBukkit.warn("dialog name collision found: $key in ${file.name} and ${it.file.name}")
                }
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("unable to load dialog. (${file.name})")
                QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
        val qnaReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            qnaMap[key] = QnA(adder,file,key,c)
        }
        val questReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                questMap.put(key,Quest(QuestAdderBukkit.Companion, file, key, c))?.let {
                    QuestAdderBukkit.warn("quest name collision found: $key in ${file.name} and ${it.file.name}")
                }
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("unable to load quest. (${file.name})")
                QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
        val npcReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                if (senderMap.containsKey(key)) throw RuntimeException("name collision found: $key")
                val npc = QuestNPC(adder, file, key, c)
                npcIdMap.put(npc.id, npc)?.let {
                    QuestAdderBukkit.warn("npc id collision found: $key in ${file.name} and ${it.file.name}")
                }
                questNpcMap.put(key, npc)?.let {
                    QuestAdderBukkit.warn("npc name collision found: $key in ${file.name} and ${it.file.name}")
                }
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("unable to load NPC. ($key in ${file.name})")
                QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
        val senderReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                if (questNpcMap.containsKey(key)) throw RuntimeException("name collision found: $key")
                val str = c.findString("Type","type") ?: throw RuntimeException("type value not found")
                senderMap[key] = DialogSenderType.valueOf(str.uppercase()).create(adder, key, c)
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("unable to load sender. ($key in ${file.name})")
                QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
        val shopReader: (File,String,ConfigurationSection) -> Unit = { file, key, c ->
            try {
                val bluePrint = ShopBlueprint(adder, file, key, c)
                val json = try {
                    QuestAdderBukkit.DB.using.loadShop(adder, bluePrint)
                } catch (ex: Exception) {
                    QuestAdderBukkit.warn("unable to load shop data: $c.get ($key in ${file.name})")
                    JsonObject()
                }
                shopMap.put(key, Shop(bluePrint, json))?.let {
                    QuestAdderBukkit.warn("shop name collision found: $key in ${file.name} and ${it.getFile().name}")
                }
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("unable to load shop. ($key in ${file.name})")
                QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
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
                    QuestAdderBukkit.warn("unable to load example file: ${it.value}")
                }
            }
        }

        QuestAdderBukkit.unregisterAll()
        actualNPCMap.values.forEach {
            it.cancel()
        }
        actionMap.values.forEach {
            it.unregister()
        }
        shopMap.values.forEach {
            it.cancel()
        }

        qnaMap.clear()
        actionMap.clear()
        questMap.clear()
        dialogMap.clear()
        questNpcMap.clear()
        actualNPCMap.clear()
        senderMap.clear()
        shopMap.clear()
        npcIdMap.clear()

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
                                "sender" -> senderReader(file,it,c)
                                "shop" -> shopReader(file,it,c)
                                else -> reader(file,it,c)
                            }
                        } ?: reader(file,it,c)
                    } ?: QuestAdderBukkit.warn("syntax error: the key '$it' is not a configuration section. (${file.name})")
                }
            }
        }

        loadConfig("actions", actionReader)
        loadConfig("dialogs", dialogReader)
        loadConfig("qnas", qnaReader)
        loadConfig("quests", questReader)
        loadConfig("npcs", npcReader)
        loadConfig("senders", senderReader)
        loadConfig("shops", shopReader)

        CitizensAPI.getNPCRegistries().forEach {
            it.forEach { npc ->
                registerNPC(npc)
            }
        }
        Bukkit.getConsoleSender().run {
            send("${actionMap.size} of actions has successfully loaded.")
            send("${dialogMap.size} of dialogs has successfully loaded.")
            send("${questMap.size} of quests has successfully loaded.")
            send("${qnaMap.size} of QnAs has successfully loaded.")
            send("${questNpcMap.size} of NPCs has successfully loaded.")
            send("${senderMap.size} of senders has successfully loaded.")
            send("${shopMap.size} of shops has successfully loaded.")
        }
        val iterator = selectedQuestMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val quest = questMap[entry.value.questKey]
            if (quest == null) iterator.remove()
            else entry.setValue(quest)
        }
        QuestAdderBukkit.task {
            QuestAdderBukkit.nms.updateCommand()
        }
    }

    override fun end(adder: QuestAdderBukkit) {
        shopMap.forEach {
            if (!QuestAdderBukkit.DB.using.saveShop(adder, it.value)) QuestAdderBukkit.warn("unable to save this shop: ${it.value.getKey()}")
        }
    }
}
