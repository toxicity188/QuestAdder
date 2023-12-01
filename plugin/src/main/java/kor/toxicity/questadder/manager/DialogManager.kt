package kor.toxicity.questadder.manager

import com.google.gson.JsonObject
import de.oliver.fancynpcs.api.FancyNpcsPlugin
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.concurrent.LazyRunnable
import kor.toxicity.questadder.api.event.*
import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.api.mechanic.DialogSender
import kor.toxicity.questadder.api.mechanic.QuestRecord
import kor.toxicity.questadder.api.mechanic.RegistrableAction
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.data.QuestData
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.hooker.npc.CitizensNPCWrapper
import kor.toxicity.questadder.hooker.npc.FancyNpcsNPCWrapper
import kor.toxicity.questadder.mechanic.dialog.Dialog
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.mechanic.npc.QuestNPC
import kor.toxicity.questadder.mechanic.qna.QnA
import kor.toxicity.questadder.mechanic.quest.Quest
import kor.toxicity.questadder.mechanic.sender.DialogSenderType
import kor.toxicity.questadder.mechanic.sender.ItemDialogSender
import kor.toxicity.questadder.shop.blueprint.ShopBlueprint
import kor.toxicity.questadder.shop.implement.Shop
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.gui.Gui
import kor.toxicity.questadder.util.gui.SubExecutor
import kor.toxicity.questadder.util.gui.player.PlayerGuiButtonType
import kor.toxicity.questadder.util.gui.player.PlayerGuiData
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.CitizensReloadEvent
import net.citizensnpcs.api.event.NPCDespawnEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object DialogManager: QuestAdderManager {

    private val dialogMap = HashMap<String, Dialog>()
    private val actionMap = HashMap<String, RegistrableAction>()
    private val questMap = HashMap<String, Quest>()
    private val qnaMap = HashMap<String, QnA>()
    private val senderMap = HashMap<String, DialogSender>()
    private val shopMap = ConcurrentHashMap<String, Shop>()

    private val questNpcMap = HashMap<String, QuestNPC>()

    private val citizensNpcIdMap = HashMap<Int, ActualNPC>()
    private val fancyNpcsNpcIdMap = HashMap<String, ActualNPC>()

    private val exampleMap = mapOf(
        "actions" to "example-action.yml",
        "dialogs" to "example-dialog.yml"
    )

    private val selectedQuestMap = ConcurrentHashMap<UUID, Quest>()

    fun getSelectedQuest(player: Player) = selectedQuestMap[player.uniqueId]

    override fun start(adder: QuestAdderBukkit) {
        val pluginManager = Bukkit.getPluginManager()
        fun activate(player: Player, actualNPC: ActualNPC) {
            val questNpc = actualNPC.questNPC
            QuestAdderBukkit.getPlayerData(player)?.let { data ->
                if (questNpc.dialogs.isNotEmpty()) {
                    val dialog = questNpc.dialogs[(data.npcIndexes.getOrPut(actualNPC.questNPC.npcKey) {
                        0
                    }).coerceAtLeast(0).coerceAtMost(questNpc.dialogs.lastIndex)]
                    if (TalkStartEvent(player, dialog, actualNPC).call()) dialog.start(player,actualNPC)
                }
            }
        }
        if (pluginManager.isPluginEnabled("Citizens")) pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun spawn(e: net.citizensnpcs.api.event.NPCSpawnEvent) {
                citizensNpcIdMap[e.npc.id]?.let {
                    senderMap[it.questNPC.npcKey] = it
                    it.startTask()
                }
            }
            @EventHandler
            fun reload(e: CitizensReloadEvent) {
                QuestAdderBukkit.reloadSync()
            }
            @EventHandler
            fun click(e: NPCRightClickEvent) {
                val player = e.clicker
                citizensNpcIdMap[e.npc.id]?.let {
                    activate(player, it)
                }
            }
            @EventHandler
            fun deSpawn(e: NPCDespawnEvent) {
                citizensNpcIdMap[e.npc.id]?.let {
                    senderMap.remove(it.questNPC.npcKey)
                    it.cancel()
                }
            }
        }, adder)
        if (pluginManager.isPluginEnabled("FancyNpcs")) pluginManager.registerEvents(object : Listener {
            fun register(npc: de.oliver.fancynpcs.api.Npc) {
                fancyNpcsNpcIdMap[npc.data.id]?.let {
                    senderMap[it.questNPC.npcKey] = it
                    it.startTask()
                }
            }
            @EventHandler
            fun spawn(e: de.oliver.fancynpcs.api.events.NpcSpawnEvent) {
                register(e.npc)
            }
            @EventHandler
            fun create(e: de.oliver.fancynpcs.api.events.NpcCreateEvent) {
                register(e.npc)
            }
            @EventHandler
            fun deSpawn(e: de.oliver.fancynpcs.api.events.NpcRemoveEvent) {
                fancyNpcsNpcIdMap[e.npc.data.id]?.let {
                    senderMap.remove(it.questNPC.npcKey)
                    it.cancel()
                }
            }
            @EventHandler
            fun interact(e: de.oliver.fancynpcs.api.events.NpcInteractEvent) {
                val player = e.player
                fancyNpcsNpcIdMap[e.npc.data.id]?.let {
                    activate(player, it)
                }
            }
        }, adder)
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
                if (!player.isSneaking) {
                    if (Dialog.skip(player)) e.isCancelled = true
                } else if (Dialog.end(player)) {
                    e.isCancelled = true
                }
            }
        },adder)
        adder.command.addApiCommand("sender", {
            aliases = arrayOf("s")
            permissions = arrayOf("questadder.sender")
        }, {
            addCommand("give") {
                aliases = arrayOf("g")
                description = "give item sender to player.".asComponent()
                length = 2
                permissions = arrayOf("questadder.sender.give")
                usage = "give ".asClearComponent().append("<player> <sender>".asComponent(NamedTextColor.AQUA))
                executor = { _, sender, args ->
                    Bukkit.getPlayer(args[0])?.let {
                        senderMap[args[1]]?.let { s ->
                            if (s is ItemDialogSender) {
                                s.give(it)
                            } else sender.warn("this sender is not item sender.")
                        } ?: sender.warn("sender not found.")
                    } ?: sender.warn("the player \"${args[0]}\" is not online.")
                }
                tabCompleter = { _, _, args ->
                    if (args.size == 2) senderMap.entries.filter {
                        it.value is ItemDialogSender && it.key.contains(args[1])
                    }.map {
                        it.key
                    } else null
                }
            }
            addCommand("run") {
                aliases = arrayOf("r")
                description = "run dialog to represented sender.".asComponent()
                length = 3
                permissions = arrayOf("questadder.sender.run")
                usage = "run ".asClearComponent().append("<player> <dialog> <sender>".asComponent(NamedTextColor.AQUA))
                executor = { _, sender, args ->
                    Bukkit.getPlayer(args[0])?.let {
                        dialogMap[args[1]]?.let { d ->
                            senderMap[args[2]]?.let { s ->
                                d.start(it,s)
                            } ?: sender.warn("the sender \"${args[2]}\" not found.")
                        } ?: sender.warn("the dialog \"${args[1]}\" not found.")
                    } ?: sender.warn("the player \"${args[0]}\" is not online.")
                }
                tabCompleter = { _, _, args ->
                    when (args.size) {
                        2 -> dialogMap.keys.filter {
                            it.contains(args[1])
                        }
                        3 -> senderMap.keys.filter {
                            it.contains(args[2])
                        }
                        else -> null
                    }
                }
            }
        }).addCommand("parse") {
            aliases = arrayOf("p")
            description = "parse result from given arguments.".asComponent()
            length = 1
            permissions = arrayOf("questadder.parse")
            usage = "parse ".asClearComponent().append("<text>".asComponent(NamedTextColor.AQUA))
            allowedSender = arrayOf(SenderType.PLAYER)
            executor = { _, sender, args ->
                val str = args.joinToString(" ")
                ComponentReader<PlayerParseEvent>(str).createComponent(
                    PlayerParseEvent(sender as Player).apply {
                        call()
                    })?.let { component ->
                    sender.info(component)
                } ?: sender.info("cannot parse this text argument.")
            }
        }.addCommand("state") {
            aliases = arrayOf("st")
            description = "sets some quest's state".asComponent()
            length = 3
            permissions = arrayOf("questadder.state")
            usage = "state ".asClearComponent().append("<player> <quest> <state>".asComponent(NamedTextColor.AQUA))
            executor = { _, sender, args ->
                Bukkit.getPlayer(args[0])?.let {
                    QuestAdderBukkit.getPlayerData(it)?.let { data ->
                        if (questMap.containsKey(args[1])) {
                            try {
                                val record = QuestRecord.valueOf(args[2].uppercase())
                                data.questVariables.getOrPut(args[1]) {
                                    QuestData(LocalDateTime.now(), record, hashMapOf())
                                }.state = record
                                sender.send("successfully changed.")
                            } catch (ex: Exception) {
                                sender.warn("unable to find that state: ${args[2]}")
                            }
                        } else {
                            sender.warn("this quest doesn't exist: ${args[1]}")
                        }
                    } ?: sender.warn("unable to load ${it.name}'s player data.")
                } ?: sender.warn("the player \"${args[0]}\" is not online")
            }
            tabCompleter = { _, _, args ->
                when (args.size) {
                    2 -> questMap.keys.filter {
                        it.contains(args[1])
                    }
                    3 -> QuestRecord.entries.map {
                        it.name.lowercase()
                    }.filter {
                        it.contains(args[2])
                    }
                    else -> null
                }
            }
        }.addApiCommand("var", {
            aliases = arrayOf("v","변수")
            permissions = arrayOf("questadder.var")
        }, {
            addCommand("set") {
                aliases = arrayOf("s","설정")
                description = "set the variable.".asComponent()
                permissions = arrayOf("questadder.var.set")
                usage = "set ".asClearComponent().append("<player> <name> <value>".asComponent(NamedTextColor.AQUA))
                length = 3
                executor = { _, sender, args ->
                    Bukkit.getPlayer(args[0])?.let { player ->
                        FunctionBuilder.evaluate(args.toList().subList(2, args.size).joinToString(" ")).apply(
                            PlayerParseEvent(
                                player
                            ).apply {
                                call()
                            })?.let {
                            QuestAdderBukkit.getPlayerData(player)?.set(args[1],it)
                            sender.send("the variable sets: ${args[1]} to $it")
                        } ?: sender.send("set failure!")
                    }
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

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
        dialogReload(adder, checker)
    }

    fun getDialog(name: String) = dialogMap[name]
    fun getAction(name: String) = actionMap[name]
    fun getQuest(name: String) = questMap[name]
    fun getQnA(name: String) = qnaMap[name]
    fun getDialogSender(name: String) = senderMap[name]

    fun getDialogKeys() = dialogMap.keys.toList()
    fun getActionKeys() = actionMap.keys.toList()
    fun getQuestKeys() = questMap.keys.toList()
    fun getQnAKeys() = qnaMap.keys.toList()
    fun getQuestNPCKeys() = questNpcMap.keys.toList()
    fun getShop(name: String) = shopMap[name]
    fun getShopKey() = shopMap.keys.toList()
    fun getQuestNPC(name: String) = questNpcMap[name]
    fun getAllNPC(): Set<ActualNPC> = HashSet<ActualNPC>().apply {
        addAll(citizensNpcIdMap.values)
    }

    private fun dialogReload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit = { _, _ -> }) {
        val npcTask = ArrayList<() -> Unit>()

        checker(0.0, "initializing dialog load...")
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
            npcTask.add {
                try {
                    if (senderMap.containsKey(key)) throw RuntimeException("name collision found: $key")
                    val npc = QuestNPC(adder, file, key, c)
                    val type = c.getString("type") ?: "citizens"
                    val old = questNpcMap[key]
                    if (old != null) {
                        throw RuntimeException("npc name collision found: $key in ${file.name} and ${old.file.name}")
                    } else {
                        val actual = when (type.lowercase()) {
                            "citizens" -> {
                                val int = c.findInt(0, "ID", "Id", "id")
                                val actual = ActualNPC(CitizensNPCWrapper(CitizensAPI.getNPCRegistries().firstNotNullOfOrNull { r ->
                                    r.getById(int)
                                } ?: throw RuntimeException("npc id \"$int\" not found.")), npc)
                                if (citizensNpcIdMap.putIfAbsent(int, actual) != null) throw RuntimeException("the npc id \"$int\" is already exist.")
                                actual
                            }
                            "fancynpcs" -> {
                                val str = c.findString("ID", "Id", "id") ?: throw RuntimeException("id value not found.")
                                val get = FancyNpcsPlugin.get().npcManager.getNpc(str)
                                val actual = ActualNPC(FancyNpcsNPCWrapper(get ?: throw RuntimeException("npc id \"$str\" not found.")), npc)
                                if (fancyNpcsNpcIdMap.putIfAbsent(get.data.id, actual) != null) throw RuntimeException("the npc id \"$str\" is already exist.")
                                actual
                            }
                            else -> throw RuntimeException("this npc type doesn't exist: $type")
                        }
                        senderMap[actual.questNPC.npcKey] = actual
                        questNpcMap[key] = npc
                    }
                } catch (ex: Throwable) {
                    QuestAdderBukkit.warn("unable to load NPC. ($key in ${file.name})")
                    QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                }
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

        //Hooker start
        citizensNpcIdMap.values.forEach {
            it.cancel()
        }
        citizensNpcIdMap.clear()
        fancyNpcsNpcIdMap.values.forEach {
            it.cancel()
        }
        fancyNpcsNpcIdMap.clear()
        //Hooker end

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
        senderMap.clear()
        shopMap.clear()
        citizensNpcIdMap.clear()

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

        checker(0.5 / 7.0, "loading actions folder...")
        loadConfig("actions", actionReader)
        checker(1.5 / 7.0, "loading dialogs folder...")
        loadConfig("dialogs", dialogReader)
        checker(2.5 / 7.0, "loading qnas folder...")
        loadConfig("qnas", qnaReader)
        checker(3.5 / 7.0, "loading quests folder...")
        loadConfig("quests", questReader)
        checker(4.5 / 7.0, "loading npcs folder...")
        loadConfig("npcs", npcReader)
        checker(5.5 / 7.0, "loading senders folder...")
        loadConfig("senders", senderReader)
        checker(6.5 / 7.0, "loading senders folder...")
        loadConfig("shops", shopReader)

        Bukkit.getConsoleSender().run {
            send("${actionMap.size} of actions has successfully loaded.")
            send("${dialogMap.size} of dialogs has successfully loaded.")
            send("${questMap.size} of quests has successfully loaded.")
            send("${qnaMap.size} of QnAs has successfully loaded.")
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
        checker(1.0, "finalizing dialog loading...")
        QuestAdderBukkit.task(null) {
            QuestAdderBukkit.nms.updateCommand()
        }
        QuestAdderBukkit.addLazyTask(object : LazyRunnable {
            override fun getDelay(): Long {
                return if (npcTask.isNotEmpty()) 5000L else 0L
            }

            override fun run() {
                npcTask.forEach {
                    it()
                }
                Bukkit.getConsoleSender().send("${questNpcMap.size} of NPCs has successfully loaded.")
            }
        })
    }

    override fun end(adder: QuestAdderBukkit) {
        shopMap.forEach {
            if (!QuestAdderBukkit.DB.using.saveShop(adder, it.value)) QuestAdderBukkit.warn("unable to save this shop: ${it.value.getKey()}")
        }
    }
}
