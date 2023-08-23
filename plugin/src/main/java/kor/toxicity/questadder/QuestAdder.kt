package kor.toxicity.questadder

import com.ticxo.playeranimator.PlayerAnimatorImpl
import com.ticxo.playeranimator.api.PlayerAnimator
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.event.ButtonGuiOpenEvent
import kor.toxicity.questadder.event.ReloadEndEvent
import kor.toxicity.questadder.event.ReloadStartEvent
import kor.toxicity.questadder.event.UserDataLoadEvent
import kor.toxicity.questadder.event.UserDataAutoSaveEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.*
import kor.toxicity.questadder.mechanic.quest.QuestState
import kor.toxicity.questadder.nms.NMS
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.SoundData
import kor.toxicity.questadder.util.TimeFormat
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.database.StandardDatabaseSupplier
import kor.toxicity.questadder.util.event.type.*
import kor.toxicity.questadder.util.gui.ButtonGui
import kor.toxicity.questadder.util.gui.player.PlayerGuiButton
import kor.toxicity.questadder.util.gui.player.PlayerGuiButtonType
import net.kyori.adventure.text.Component
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.EnumMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class QuestAdder: JavaPlugin() {
    companion object {
        lateinit var nms: NMS
            private set
        lateinit var animator: PlayerAnimator
            private set
        private lateinit var plugin: QuestAdder

        private val playerThreadMap = ConcurrentHashMap<UUID,PlayerThread>()

        @Internal
        fun send(message: String) = plugin.logger.info(message)
        @Internal
        fun warn(message: String) = plugin.logger.warning(message)


        @Internal
        fun task(action: () -> Unit) = Bukkit.getScheduler().runTask(plugin,action)
        @Internal
        fun asyncTask(action: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin,action)
        @Internal
        fun taskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLater(plugin,action,delay)
        @Internal
        fun taskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimer(plugin,action,delay,period)
        @Internal
        fun asyncTaskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,action,delay,period)

        @Internal
        fun getPlayerData(player: Player) = playerThreadMap[player.uniqueId]?.data
        @Internal
        fun reload(callback: (Long) -> Unit) = plugin.reload(callback)
        @Internal
        fun reloadSync() = plugin.reloadSync()

        private val managerList = mutableListOf(
            ResourcePackManager,
            NavigationManager,
            SlateManager,
            LocationManager,
            GuiManager,
            ItemManager,
            GestureManager,
            DialogManager
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
        var questSuffix = listOf<Component>()
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
                PlayerGuiButtonType.values().forEach { type ->
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


    override fun onEnable() {
        plugin = this
        try {
            animator = PlayerAnimatorImpl.initialize(this)
            nms = Class.forName("kor.toxicity.questadder.nms.${Bukkit.getServer()::class.java.`package`.name.split(".")[3]}.NMSImpl").getConstructor().newInstance() as NMS
        } catch (ex: Exception) {
            warn("unsupported version found.")
            warn("plugin disabled.")
            Bukkit.getPluginManager().disablePlugin(this)
            return
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
        if (pluginManager.isPluginEnabled("MagicSpells")) ActionBuilder.run {
            addEvent("spellcast", EventSpellCast::class.java)
            addEvent("spelltarget", EventSpellTarget::class.java)
            addEvent("spelldamage", EventSpellDamage::class.java)
            addEvent("spelllearn", EventSpellLearn::class.java)
            addEvent("spellforget", EventSpellForget::class.java)
            addEvent("buffstart", EventBuffStart::class.java)
            addEvent("buffend", EventBuffEnd::class.java)
        }
        if (pluginManager.isPluginEnabled("MythicMobs")) ActionBuilder.run {
            addEvent("mythicdamage", EventMythicDamage::class.java)
            addEvent("mythicheal", EventMythicHeal::class.java)
            addEvent("mythickill", EventMythicKill::class.java)
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
        loadDatabase()
        managerList.forEach {
            it.start(this)
        }
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                asyncTask {
                    playerThreadMap[player.uniqueId] = PlayerThread(player)
                }
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                asyncTask {
                    playerThreadMap.remove(player.uniqueId)?.let {
                        it.save()
                        it.cancel()
                    }
                }
            }
        },this)
        Metrics(this,19565)
        load()
        send("plugin enabled.")
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
        managerList.forEach {
            it.reload(this)
        }
        loadFile("config")?.let { config ->
            Config.reload(config)
        }
        var task: (() -> Unit)?
        do {
            task = lazyTaskCache.poll()?.apply {
                invoke()
            }
        } while (task != null)
    }
    private fun reloadSync() {
        loadDatabase()
        load()
    }
    private fun loadDatabase() {
        loadFile("database")?.let { database ->
            DB.reload(database)
        }
    }
    private fun reload(callback: (Long) -> Unit) {
        ReloadStartEvent().callEvent()
        asyncTask {
            var time = System.currentTimeMillis()
            loadDatabase()
            load()
            time = System.currentTimeMillis() - time
            task {
                callback(time)
                ReloadEndEvent().callEvent()
            }
        }
    }

    fun loadFolder(folderName: String, action: (File,ConfigurationSection) -> Unit) {
        File(dataFolder.apply {
            mkdir()
        },folderName).run {
            mkdir()
            listFiles()?.forEach {
                if (it.extension == "yml") try {
                    YamlConfiguration().run {
                        load(it)
                        action(it,this)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    warn("unable to read this file: ${it.name}")
                }
            }
        }
    }
    private fun loadFile(fileName: String) = try {
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
        val data = DB.using.load(this@QuestAdder,player)
        private val task = asyncTaskTimer(Config.autoSaveTime,Config.autoSaveTime) {
            save()
            task {
                UserDataAutoSaveEvent(player, data).callEvent()
            }
        }
        private val remove = asyncTaskTimer(60 * 20, 60 * 20) {
            data.questVariables.entries.removeIf {
                val quest = DialogManager.getQuest(it.key)
                quest != null && quest.left > 0 && it.value.state == QuestState.HAS && ChronoUnit.MINUTES.between(it.value.time,LocalDateTime.now()) > quest.left
            }
        }
        init {
            task {
                UserDataLoadEvent(player,data).callEvent()
            }
        }
        fun save() {
            DB.using.save(this@QuestAdder,player,data)
        }
        fun cancel() {
            task.cancel()
            remove.cancel()
        }
    }
}