package kor.toxicity.questadder

import com.ticxo.playeranimator.PlayerAnimatorImpl
import com.ticxo.playeranimator.api.PlayerAnimator
import com.ticxo.playeranimator.api.model.ModelManager
import com.ticxo.playeranimator.api.model.player.PlayerModel
import kor.toxicity.questadder.api.APIManager
import kor.toxicity.questadder.api.QuestAdder
import kor.toxicity.questadder.api.QuestAdderAPI
import kor.toxicity.questadder.api.QuestAdderPlugin
import kor.toxicity.questadder.api.event.*
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.data.PlayerData
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.*
import kor.toxicity.questadder.nms.NMS
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.TimeFormat
import kor.toxicity.questadder.util.action.ActCast
import kor.toxicity.questadder.util.action.ActSkill
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.database.StandardDatabaseSupplier
import kor.toxicity.questadder.api.mechanic.AbstractEvent
import kor.toxicity.questadder.api.mechanic.QuestRecord
import kor.toxicity.questadder.api.util.SoundData
import kor.toxicity.questadder.platform.PaperPlatformAdapter
import kor.toxicity.questadder.platform.PlatformAdapter
import kor.toxicity.questadder.platform.SpigotPlatformAdapter
import kor.toxicity.questadder.util.event.itemsadder.EventCustomBlockBreak
import kor.toxicity.questadder.util.event.itemsadder.EventCustomBlockClick
import kor.toxicity.questadder.util.event.itemsadder.EventCustomBlockPlace
import kor.toxicity.questadder.util.event.magicspells.*
import kor.toxicity.questadder.util.event.mmocore.EventMMOAttributeUse
import kor.toxicity.questadder.util.event.mmocore.EventMMOChangeClass
import kor.toxicity.questadder.util.event.mmocore.EventMMOCombat
import kor.toxicity.questadder.util.event.mmocore.EventMMOExpGain
import kor.toxicity.questadder.util.event.mmocore.EventMMOGuildChat
import kor.toxicity.questadder.util.event.mmocore.EventMMOItemLock
import kor.toxicity.questadder.util.event.mmocore.EventMMOItemUnlock
import kor.toxicity.questadder.util.event.mmocore.EventMMOLevelUp
import kor.toxicity.questadder.util.event.mmocore.EventMMOPartyChat
import kor.toxicity.questadder.util.event.mmoitems.EventMMOApplyGemStone
import kor.toxicity.questadder.util.event.mmoitems.EventMMOApplySoulbound
import kor.toxicity.questadder.util.event.mmoitems.EventMMOBreakSoulbound
import kor.toxicity.questadder.util.event.mmoitems.EventMMOConsume
import kor.toxicity.questadder.util.event.mmoitems.EventMMOReforge
import kor.toxicity.questadder.util.event.mmoitems.EventMMOUnsocketGemStone
import kor.toxicity.questadder.util.event.mythiclib.EventMMOBlock
import kor.toxicity.questadder.util.event.mythiclib.EventMMOCastSkill
import kor.toxicity.questadder.util.event.mythiclib.EventMMODodge
import kor.toxicity.questadder.util.event.mythiclib.EventMMOParry
import kor.toxicity.questadder.util.event.mythicmobs.EventMythicDamage
import kor.toxicity.questadder.util.event.mythicmobs.EventMythicHeal
import kor.toxicity.questadder.util.event.mythicmobs.EventMythicKill
import kor.toxicity.questadder.util.event.oraxen.*
import kor.toxicity.questadder.util.event.superiorskyblock.*
import kor.toxicity.questadder.util.event.worldguard.EventRegionEnter
import kor.toxicity.questadder.util.event.worldguard.EventRegionExit
import kor.toxicity.questadder.util.gui.ButtonGui
import kor.toxicity.questadder.util.gui.player.PlayerGuiButton
import kor.toxicity.questadder.util.gui.player.PlayerGuiButtonType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ThreadLocalRandom

class QuestAdderBukkit: JavaPlugin(), QuestAdderPlugin {
    companion object: QuestAdder {

        const val VERSION = "1.1.9"

        private val listener = object : Listener {
        }

        lateinit var nms: NMS
            private set
        var animator: PlayerAnimator? = null
            private set

        var reloaded = false
            private set
        lateinit var audience: BukkitAudiences
            private set
        val platform: PlatformAdapter = if (Bukkit.getConsoleSender() is Audience) PaperPlatformAdapter() else SpigotPlatformAdapter()

        private lateinit var plugin: QuestAdderBukkit

        private val playerThreadMap = ConcurrentHashMap<UUID,PlayerThread>()

        fun unregisterAll() {
            HandlerList.unregisterAll(listener)
        }

        @Internal
        fun send(message: String) = plugin.logger.info(message)
        @Internal
        fun warn(message: String) = plugin.logger.warning(message)

        override fun addLazyTask(runnable: Runnable) {
            plugin.addLazyTask {
                runnable.run()
            }
        }

        override fun getAPIManager(): APIManager {
            return QuestAdderAPIBukkit
        }

        override fun <T: Event> registerEvent(event: AbstractEvent<T>, clazz: Class<T>) {
            Bukkit.getPluginManager().registerEvent(clazz, listener, EventPriority.MONITOR,{ _, e ->
                if (clazz.isAssignableFrom(e::class.java) && ThreadLocalRandom.current().nextDouble(100.0) <= event.chance) event.invoke(clazz.cast(e))
            }, plugin)
        }

        override fun getPlugin(): QuestAdderPlugin {
            return plugin
        }

        @Internal
        fun task(action: () -> Unit) = Bukkit.getScheduler().runTask(plugin,action)
        @Internal
        fun asyncTask(action: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin,action)
        @Internal
        fun taskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLater(plugin,action,delay)
        @Internal
        fun taskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimer(plugin,action,delay,period)
        @Internal
        fun asyncTaskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,action,delay)
        @Internal
        fun asyncTaskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,action,delay,period)

        @Internal
        fun getPlayerData(player: Player) = playerThreadMap[player.uniqueId]?.data
        @Internal
        fun getPlayerData(player: OfflinePlayer, action: (PlayerData) -> Unit) {
            asyncTask {
                val data = playerThreadMap[player.uniqueId]?.data ?: DB.using.load(plugin, player)
                action(data)
                DB.using.save(plugin, player, data)
            }
        }
        @Internal
        fun reload(callback: (Long) -> Unit) = plugin.reload(callback)
        @Internal
        fun reloadSync() = plugin.reloadSync()

        private val managerList = mutableListOf(
            ResourcePackManager,
            UUIDManager,
            HookerManager,
            CallbackManager,
            NavigationManager,
            SlateManager,
            LocationManager,
            GuiManager,
            ItemManager,
            SkinManager,
            EntityManager,
            GestureManager,
            DialogManager,
            ShopManager
        )
    }
    object Config {
        var defaultTypingSpeed = 1L
            private set
        var autoSaveTime = 6000L
            private set
        var defaultDialogItem = Material.ENCHANTED_BOOK
            private set
        var playerGui = ButtonGui<ButtonGuiOpenEvent>(
            4,
            ComponentReader("quest"),
            emptyMap()
        )
        private val playerGuiButton = EnumMap<PlayerGuiButtonType,PlayerGuiButton<ButtonGuiOpenEvent>>(PlayerGuiButtonType::class.java)
        var playerGuiStartIndex = 1
            private set
        var playerGuiMaxIndex = 1
            private set
        var defaultResourcePackItem = Material.ENDER_EYE
            private set
        var navigationGuiName: Component = Component.empty()
            private set
        var defaultTypingSound = SoundData("block.stone_button.click_on",1.0F,0.7F)
            private set
        var timeFormat = TimeFormat(MemoryConfiguration())
            private set
        var questSuffix = emptyList<Component>()
            private set
        var zipResourcePack = true
            private set
        var importOtherResourcePack = emptyList<String>()
            private set
        fun getPlayerGuiButton(type: PlayerGuiButtonType) = playerGuiButton[type]
        internal fun reload(section: ConfigurationSection) {
            defaultTypingSpeed = section.getLong("default-typing-speed",1L)
            autoSaveTime = section.getLong("auto-save-time",300L) * 20
            section.getString("default-dialog-item")?.let {
                try {
                    defaultDialogItem = Material.valueOf(it.uppercase())
                } catch (ex: Exception) {
                    warn("not found error: unable to find material name \"$it\"")
                }
            }
            section.getConfigurationSection("player-gui")?.let {
                try {
                    playerGui = ButtonGui(it)
                } catch (ex: Exception) {
                    warn("unable to load player gui.")
                    warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                }
            } ?: run {
                playerGui = ButtonGui(
                    4,
                    ComponentReader("quest"),
                    emptyMap()
                )
            }
            section.getString("default-resource-pack-item")?.let {
                try {
                    defaultResourcePackItem = Material.valueOf(it.uppercase())
                } catch (ex: Exception) {
                    warn("not found error: unable to find material name \"$it\"")
                }
            }
            section.getString("navigation-gui-name")?.let {
                navigationGuiName = it.colored()
            }
            playerGuiButton.clear()
            section.getConfigurationSection("player-gui-layout")?.let {
                PlayerGuiButtonType.entries.forEach { type ->
                    it.getConfigurationSection(type.name.lowercase().replace('_','-'))?.let { config ->
                        try {
                            playerGuiButton[type] = PlayerGuiButton(config)
                        } catch (ex: Exception) {
                            warn("unable to load button: ${type.name.lowercase()}")
                            warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                        }
                    } ?: warn("syntax error: the key ${type.name.lowercase()} is not a configuration section.")
                }
                playerGuiStartIndex = it.getInt("start-index").coerceAtLeast(0).coerceAtMost(5)
                playerGuiMaxIndex = it.getInt("max-index").coerceAtLeast(1).coerceAtMost(6 - playerGuiStartIndex)
            }
            section.getAsSoundData("default-typing-sound")?.let {
                defaultTypingSound = it
            }
            section.getConfigurationSection("time-format")?.let {
                timeFormat = TimeFormat(it)
            }
            section.getAsStringList("quest-suffix")?.let {
                questSuffix = it.map { s ->
                    s.colored()
                }
            }
            zipResourcePack = section.getBoolean("zip-resource-pack", true)
            section.getAsStringList("import-other-resource-pack")?.let {
                importOtherResourcePack = it
            }
        }
    }
    object Prefix {

        var plugin: Component = Component.empty()
            private set
        var info: Component = Component.empty()
            private set
        var warn: Component = Component.empty()
            private set
        var condition: Component = Component.empty()
            private set
        var conditionLore: Component = Component.empty()
            private set
        var reward: Component = Component.empty()
            private set
        var rewardLore: Component = Component.empty()
            private set
        var recommend: Component = Component.empty()
            private set
        var left: Component = Component.empty()
            private set
        internal fun reload(section: ConfigurationSection) {
            section.getString("plugin")?.let { p ->
                plugin = p.colored()
            }
            section.getString("info")?.let { i ->
                info = i.colored()
            }
            section.getString("warn")?.let { w ->
                warn = w.colored()
            }
            section.getString("condition")?.let { c ->
                condition = c.colored()
            }
            section.getString("condition-lore")?.let { c ->
                conditionLore = c.colored()
            }
            section.getString("reward")?.let { c ->
                reward = c.colored()
            }
            section.getString("recommend")?.let { c ->
                recommend = c.colored()
            }
            section.getString("reward-lore")?.let { c ->
                rewardLore = c.colored()
            }
            section.getString("left")?.let { c ->
                left = c.colored()
            }
        }
    }
    object Suffix {
        var exp: Component = Component.empty()
            private set
        var money: Component = Component.empty()
            private set
        internal fun reload(section: ConfigurationSection) {
            section.getString("exp")?.let {
                exp = it.colored()
            }
            section.getString("money")?.let {
                money = it.colored()
            }
        }
    }
    object DB {
        var using = StandardDatabaseSupplier.YML.supply(MemoryConfiguration())
            private set
        internal fun reload(section: ConfigurationSection) {
            val info = section.getConfigurationSection("info") ?: return
            section.getString("using")?.let { s ->
                try {
                    val newDatabase = StandardDatabaseSupplier.valueOf(s.uppercase()).supply(info)
                    using.close()
                    using = newDatabase
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    warn("unable to load the database.")
                }
            }
        }
    }

    val command = CommandAPI("qa")
        .addCommand("reload") {
            aliases = arrayOf("re","rl","리로드")
            description = "reload this plugin."
            usage = "reload"
            opOnly = true
            executor = { sender, _ ->
                reload {
                    sender.info("reload completed. ($it ms)")
                }
            }
        }
        .addCommand("run") {
            aliases = arrayOf("실행")
            description = "run the action."
            usage = "run <player> <action> [args]"
            opOnly = true
            allowedSender = arrayOf(SenderType.PLAYER)
            length = 2
            executor = { sender, args ->
                Bukkit.getPlayer(args[1])?.let { player ->
                    val arg = args.toMutableList().apply {
                        removeAt(0)
                        removeAt(0)
                        removeAt(0)
                    }.toTypedArray()
                    DialogManager.getAction(args[2])?.apply(player, *arg) ?: sender.warn("unable to find the action: ${args[2]}")
                } ?: sender.warn("unable to find that player: ${args[1]}")
            }
            tabComplete = { _, args ->
                if (args.size == 3) DialogManager.getActionKeys() else null
            }
        }
        .addCommand("index") {
            aliases = arrayOf("i","인덱스")
            description = "set the NPC's index."
            usage = "index <player> <NPC key> <index>"
            opOnly = true
            allowedSender = arrayOf(SenderType.PLAYER)
            length = 3
            executor = { sender, args ->
                Bukkit.getPlayer(args[1])?.let { player ->
                    DialogManager.getQuestNPC(args[2])?.let {
                        try {
                            it.setIndex(player, args[3].toInt())
                        } catch (ex: Exception) {
                            sender.warn("this is not an integer: ${args[3]}")
                        }
                    } ?: sender.warn("NPC not found: ${args[2]}")
                } ?: sender.warn("player not found: ${args[1]}")
            }
            tabComplete = { _, args ->
                if (args.size == 3) DialogManager.getQuestNPCKeys() else null
            }
        }
        .addCommandAPI("savepoint", arrayOf("sp"), "savepoint-related command.", true, CommandAPI("qa sp")
            .addCommand("save") {
                aliases = arrayOf("s", "저장")
                description = "save current player data to save point."
                usage = "save <name>"
                opOnly = true
                allowedSender = arrayOf(SenderType.PLAYER)
                length = 1
                executor = { sender, args ->
                    playerThreadMap[(sender as Player).uniqueId]?.let {
                        asyncTask {
                            val name = args.toList().subList(1, args.size).joinToString(" ")
                            try {
                                (it.data.serialize() as YamlConfiguration).run {
                                    save(File(getSavePointFolder(), "$name.yml").apply {
                                        if (!exists()) createNewFile()
                                    })
                                }
                                sender.info("successfully make a savepoint: $name")
                            } catch (ex: Exception) {
                                sender.warn("unable to make a savepoint: $name")
                            }
                        }
                    } ?: sender.warn("your thread doesn't exist.")
                }
            }
            .addCommand("load") {
                aliases = arrayOf("l", "불러오기")
                description = "load specific savepoint to some player."
                usage = "load <player> <name> "
                opOnly = true
                length = 1
                executor = { sender, args ->
                    Bukkit.getPlayer(args[1])?.let {
                        playerThreadMap[it.uniqueId]?.let { thread ->
                            asyncTask {
                                val name = args.toList().subList(2, args.size).joinToString(" ")
                                try {
                                    val file = File(getSavePointFolder(), "$name.yml")
                                    if (!file.exists()) {
                                        sender.warn("this savepoint doesn't exist: $name")
                                    } else {
                                        thread.data = PlayerData.deserialize(YamlConfiguration().apply {
                                            load(file)
                                        })
                                        sender.info("successfully loaded: $name")
                                    }
                                } catch (ex: Exception) {
                                    sender.warn("unable to load a savepoint: $name")
                                }
                            }
                        } ?: sender.warn("${it.name}'s thread doesn't exist.")
                    } ?: sender.warn("this player does not exist: ${args[2]}")
                }
                tabComplete = { _, args ->
                    if (args.size == 3) getSavePointFolder().listFiles()?.map {
                        it.nameWithoutExtension
                    }?.filter {
                        it.startsWith(args[2])
                    } else null
                }
            }
        )
    private fun getSavePointFolder() = File(File(dataFolder.apply {
        if (!exists()) mkdir()
    }, ".data").apply {
        if (!exists()) mkdir()
    },".savepoint").apply {
        if (!exists()) mkdir()
    }

    override fun onEnable() {
        plugin = this
        QuestAdderAPI.setInstance(Companion)
        audience = BukkitAudiences.create(this)
        try {
            nms = Class.forName("kor.toxicity.questadder.nms.${Bukkit.getServer()::class.java.`package`.name.split(".")[3]}.NMSImpl").getConstructor().newInstance() as NMS
        } catch (ex: Exception) {
            warn("unsupported version found.")
            warn("plugin disabled.")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        try {
            animator = PlayerAnimatorImpl.initialize(this).apply {
                modelManager = object : ModelManager() {
                    private val playerMap = ConcurrentHashMap<UUID, PlayerModel>()
                    init {
                        asyncTaskTimer(1,1) {
                            val iterator = playerMap.values.iterator()
                            while (iterator.hasNext()) {
                                val next = iterator.next()
                                if (!next.update()) {
                                    iterator.remove()
                                    next.despawn()
                                }
                            }
                        }
                    }
                    override fun getPlayerModel(entity: Entity): PlayerModel? {
                        return playerMap[entity.uniqueId]
                    }

                    override fun unregisterModel(entity: Entity) {
                        playerMap.remove(entity.uniqueId)
                    }

                    override fun registerModel(model: PlayerModel) {
                        playerMap[model.base.uniqueId] = model
                    }
                }
            }
        } catch (ex: Exception) {
            warn("this version does not support gesture.")
        }
        getCommand("questadder")?.setExecutor(command.createTabExecutor())
        val pluginManager = Bukkit.getPluginManager()
        pluginManager.getPlugin("PlaceholderAPI")?.let {
            getResource("Expansion-questadder.patch")?.buffered()?.use { stream ->
                try {
                    File(File(it.dataFolder, "expansions").apply {
                        mkdir()
                    }, "Expansion.questadder.jar").outputStream().buffered().use {
                        stream.copyTo(it)
                    }
                } catch (ex: Exception) {
                    warn("unable to unzip QuestAdder's expansion")
                }
            }
        }
        if (pluginManager.isPluginEnabled("WorldGuard")) {
            managerList.add(WorldGuardManager)
            ActionBuilder.run {
                addEvent("enter", EventRegionEnter::class.java)
                addEvent("exit", EventRegionExit::class.java)
            }
        }
        addActionAndEvent()
        loadDatabase()
        managerList.forEach {
            it.start(this)
        }
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                nms.getChannel().inject(player)
                asyncTask {
                    playerThreadMap[player.uniqueId] = PlayerThread(player)
                }
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                nms.getChannel().uninject(player)
                asyncTask {
                    playerThreadMap.remove(player.uniqueId)?.let {
                        it.save()
                        it.cancel()
                    }
                }
            }
        },this)
        Metrics(this,19565)
        task {
            PluginLoadStartEvent().call()
            load()
            PluginLoadEndEvent().call()
            send("plugin enabled.")
            send("current platform: ${platform.getTargetPlatformName()}")
            try {
                val get = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=112227/"))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString()).body()
                if (VERSION != get) {
                    warn("new version found: $get")
                    warn("download: https://www.spigotmc.org/resources/questadder.112227/")
                    pluginManager.registerEvents(object : Listener {
                        @EventHandler
                        fun join(e: PlayerJoinEvent) {
                            val player = e.player
                            if (player.isOp) {
                                player.info("new version found: $get")
                                player.info("download: https://www.spigotmc.org/resources/questadder.112227/".asClearComponent().clickEvent(
                                    ClickEvent.openUrl("https://www.spigotmc.org/resources/questadder.112227/")))
                            }
                        }
                    },this)
                }
            } catch (ex: Exception) {
                warn("unable to check for updates: ${ex.message ?: ex.javaClass.simpleName}")
            }
        }
    }

    override fun onDisable() {
        playerThreadMap.values.forEach {
            it.save()
        }
        managerList.forEach {
            it.end(this)
        }
        send("plugin disabled.")
    }

    private val lazyTaskCache = ConcurrentLinkedQueue<() -> Unit>()

    fun addLazyTask(action: () -> Unit) {
        lazyTaskCache.add(action)
    }
    private fun load() {
        loadFile("prefix")?.let { prefix ->
            Prefix.reload(prefix)
        }
        loadFile("suffix")?.let { suffix ->
            Suffix.reload(suffix)
        }
        loadFile("config")?.let { config ->
            Config.reload(config)
        }
        managerList.forEach {
            it.reload(this)
        }
        var task: (() -> Unit)?
        do {
            task = lazyTaskCache.poll()?.apply {
                invoke()
            }
        } while (task != null)
    }
    private fun reloadSync() {
        if (reloaded) {
            warn("plugin is still on reload.")
            return
        }
        reloaded = true
        loadDatabase()
        load()
        reloaded = false
    }
    private fun loadDatabase() {
        loadFile("database")?.let { database ->
            DB.reload(database)
        }
    }
    private fun reload(callback: (Long) -> Unit) {
        ReloadStartEvent().call()
        asyncTask {
            var time = System.currentTimeMillis()
            reloadSync()
            time = System.currentTimeMillis() - time
            task {
                callback(time)
                ReloadEndEvent().call()
            }
        }
    }

    fun loadFolder(folderName: String, action: (File,ConfigurationSection) -> Unit) {
        File(dataFolder.apply {
            mkdir()
        },folderName).loadYamlFolder(action)
    }
    fun loadFile(fileName: String) = try {
        YamlConfiguration().apply {
            load(File(dataFolder.apply {
                mkdir()
            },"$fileName.yml").apply {
                if (!exists()) saveResource("$fileName.yml",false)
            })
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        warn("unable to read this file: $fileName.yml")
        null
    }

    private inner class PlayerThread(val player: Player) {
        var data = DB.using.load(this@QuestAdderBukkit,player)
        private val task = asyncTaskTimer(Config.autoSaveTime,Config.autoSaveTime) {
            save()
            task {
                UserDataAutoSaveEvent(player, data).call()
            }
        }
        private val remove = asyncTaskTimer(60 * 20, 60 * 20) {
            data.questVariables.entries.removeIf {
                val quest = DialogManager.getQuest(it.key)
                quest != null && quest.left > 0 && it.value.state == QuestRecord.HAS && ChronoUnit.MINUTES.between(it.value.time,LocalDateTime.now()) > quest.left
            }
        }
        init {
            task {
                UserDataLoadEvent(player, data).call()
            }
        }
        fun save() {
            DB.using.save(this@QuestAdderBukkit,player,data)
        }
        fun cancel() {
            task.cancel()
            remove.cancel()
        }
    }

    private fun addActionAndEvent() {
        val pluginManager = Bukkit.getPluginManager()
        if (pluginManager.isPluginEnabled("MagicSpells")) ActionBuilder.run {
            addEvent("spellcast", EventSpellCast::class.java)
            addEvent("spelltarget", EventSpellTarget::class.java)
            addEvent("spelldamage", EventSpellDamage::class.java)
            addEvent("spelllearn", EventSpellLearn::class.java)
            addEvent("spellforget", EventSpellForget::class.java)
            addEvent("buffstart", EventBuffStart::class.java)
            addEvent("buffend", EventBuffEnd::class.java)

            addAction("cast", ActCast::class.java)
        }
        if (pluginManager.isPluginEnabled("MythicMobs")) ActionBuilder.run {
            addEvent("mythicdamage", EventMythicDamage::class.java)
            addEvent("mythicheal", EventMythicHeal::class.java)
            addEvent("mythickill", EventMythicKill::class.java)

            addAction("skill", ActSkill::class.java)
        }
        if (pluginManager.isPluginEnabled("MythicLib")) ActionBuilder.run {
            addEvent("mmoblock", EventMMOBlock::class.java)
            addEvent("mmododge", EventMMODodge::class.java)
            addEvent("mmoparry", EventMMOParry::class.java)
            addEvent("mmocastskill", EventMMOCastSkill::class.java)
        }
        if (pluginManager.isPluginEnabled("MMOItems")) ActionBuilder.run {
            addEvent("mmoreforge", EventMMOReforge::class.java)
            addEvent("mmoapplygemstone", EventMMOApplyGemStone::class.java)
            addEvent("mmounsocketgemstone", EventMMOUnsocketGemStone::class.java)
            addEvent("mmoapplysoulbound", EventMMOApplySoulbound::class.java)
            addEvent("mmobreaksoulbound", EventMMOBreakSoulbound::class.java)
            addEvent("mmoconsume", EventMMOConsume::class.java)
        }
        if (pluginManager.isPluginEnabled("MMOCore")) ActionBuilder.run {
            addEvent("mmopartychat", EventMMOPartyChat::class.java)
            addEvent("mmoguildchat", EventMMOGuildChat::class.java)
            addEvent("mmocombat", EventMMOCombat::class.java)
            addEvent("mmolevelup", EventMMOLevelUp::class.java)
            addEvent("mmoexpgain", EventMMOExpGain::class.java)
            addEvent("mmochangeclass", EventMMOChangeClass::class.java)
            addEvent("mmoattributeuse", EventMMOAttributeUse::class.java)
            addEvent("mmoitemlock", EventMMOItemLock::class.java)
            addEvent("mmoitemunlock", EventMMOItemUnlock::class.java)
        }
        if (pluginManager.isPluginEnabled("SuperiorSkyblock2")) ActionBuilder.run {
            addEvent("islandopen", EventIslandOpen::class.java)
            addEvent("islandclose", EventIslandClose::class.java)
            addEvent("islandquit", EventIslandQuit::class.java)
            addEvent("islandrate", EventIslandRate::class.java)
            addEvent("islandenter", EventIslandEnter::class.java)
            addEvent("islandcreate", EventIslandCreate::class.java)
            addEvent("islandchat", EventIslandChat::class.java)
            addEvent("islandjoin", EventIslandJoin::class.java)
            addEvent("islandinvite", EventIslandInvite::class.java)
        }
        if (pluginManager.isPluginEnabled("Oraxen")) ActionBuilder.run {
            addEvent("oraxenfurniturebreak", EventOraxenFurnitureBreak::class.java)
            addEvent("oraxennotebreak", EventOraxenNoteBreak::class.java)
            addEvent("oraxenstringbreak", EventOraxenStringBreak::class.java)

            addEvent("oraxenfurnitureplace", EventOraxenFurniturePlace::class.java)
            addEvent("oraxennoteplace", EventOraxenNotePlace::class.java)
            addEvent("oraxenstringplace", EventOraxenStringPlace::class.java)

            addEvent("oraxenfurnitureclick", EventOraxenFurnitureClick::class.java)
            addEvent("oraxennoteclick", EventOraxenNoteClick::class.java)
            addEvent("oraxenstringclick", EventOraxenStringClick::class.java)
        }
        if (pluginManager.isPluginEnabled("ItemsAdder")) ActionBuilder.run {
            addEvent("customblockbreak", EventCustomBlockBreak::class.java)
            addEvent("customblockclick", EventCustomBlockClick::class.java)
            addEvent("customblockplace", EventCustomBlockPlace::class.java)
        }
    }
}
