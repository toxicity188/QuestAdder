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
import kor.toxicity.questadder.nms.NMS
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.database.StandardDatabaseSupplier
import kor.toxicity.questadder.util.gui.ButtonGui
import kor.toxicity.questadder.util.gui.player.PlayerGuiButton
import kor.toxicity.questadder.util.gui.player.PlayerGuiButtonType
import net.kyori.adventure.text.Component
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
import java.io.File
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

        fun send(message: String) = plugin.logger.info(message)
        fun warn(message: String) = plugin.logger.warning(message)


        fun task(action: () -> Unit) = Bukkit.getScheduler().runTask(plugin,action)
        fun asyncTask(action: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin,action)
        fun taskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLater(plugin,action,delay)
        fun asyncTaskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,action,delay)
        fun taskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimer(plugin,action,delay,period)
        fun asyncTaskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,action,delay,period)

        fun getPlayerData(player: Player) = playerThreadMap[player.uniqueId]?.data
        fun reload(callback: (Long) -> Unit) = plugin.reload(callback)
        fun reloadSync() = plugin.reloadSync()

        fun addPlayerVariable(player: Player, name: String, value: Any) {
            playerThreadMap[player.uniqueId]?.run {
                data.set(name,value)
            }
        }
        fun removePlayerVariable(player: Player, name: String) {
            playerThreadMap[player.uniqueId]?.run {
                data.remove(name)
            }
        }

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
                    using = StandardDatabaseSupplier.valueOf(s.uppercase()).supply(info)
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
        Bukkit.getPluginManager().getPlugin("PlaceholderAPI")?.let {
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
        loadConfig()
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
        reloadConfig()
        load()
    }
    private fun loadConfig() {
        loadFile("config")?.let { config ->
            Config.reload(config)
        }
        loadFile("prefix")?.let { prefix ->
            Prefix.reload(prefix)
        }
        loadFile("database")?.let { database ->
            DB.reload(database)
        }
        loadFile("suffix")?.let { suffix ->
            Suffix.reload(suffix)
        }
    }
    private fun reload(callback: (Long) -> Unit) {
        ReloadStartEvent().callEvent()
        asyncTask {
            var time = System.currentTimeMillis()
            loadConfig()
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
        val data = DB.using.load(this@QuestAdder,player)
        private val task = asyncTaskTimer(Config.autoSaveTime,Config.autoSaveTime) {
            save()
            task {
                UserDataAutoSaveEvent(player, data).callEvent()
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
        }
    }
}