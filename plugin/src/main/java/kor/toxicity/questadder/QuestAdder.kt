package kor.toxicity.questadder

import com.ticxo.playeranimator.PlayerAnimatorImpl
import com.ticxo.playeranimator.api.PlayerAnimator
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.event.ReloadEndEvent
import kor.toxicity.questadder.event.ReloadStartEvent
import kor.toxicity.questadder.event.UserDataLoadEvent
import kor.toxicity.questadder.event.UserDataAutoSaveEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.*
import kor.toxicity.questadder.nms.NMS
import kor.toxicity.questadder.util.database.StandardDatabaseSupplier
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
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

        private val managerList = mutableListOf(
            SlateManager,
            FontManager,
            LocationManager,
            GuiManager,
            ItemManager,
            GestureManager,
            DialogManager,
            NPCManager
        )
    }
    object Config {
        var defaultTypingSpeed = 1L
            private set
        var autoSaveTime = 6000L
            private set
        internal fun reload(section: ConfigurationSection) {
            defaultTypingSpeed = section.getLong("default-typing-speed",1L)
            autoSaveTime = section.getLong("auto-save-time",300L) * 20
        }
    }
    object Prefix {

        var plugin: Component = "[QuestAdder] ".asComponent(GOLD).clear().append(Component.empty().color(WHITE))
            private set
        var info: Component = " [!] ".asComponent(GOLD).clear().append(Component.empty().color(WHITE))
            private set
        var warn: Component = " [!] ".asComponent(RED).clear().append(Component.empty().color(WHITE))
            private set
        var condition: Component = " [!] Condition".asComponent(YELLOW).clear()
            private set
        var conditionLore: Component = " [!] ".asComponent(YELLOW).clear().append(Component.empty().color(WHITE))
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
            warn("unsupported version found!.")
            warn("plugin will be disabled.")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        getCommand("questadder")?.setExecutor(command.createTabExecutor())
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
        loadFile("config")?.let { config ->
            Config.reload(config)
        }
        loadFile("prefix")?.let { prefix ->
            Prefix.reload(prefix)
        }
        loadFile("database")?.let { database ->
            DB.reload(database)
        }
        managerList.forEach {
            it.reload(this)
        }
        var task: (() -> Unit)?
        do {
            task = lazyTaskCache.poll()
            task?.let {
                it()
            }
        } while (task != null)
    }
    private fun reload(callback: (Long) -> Unit) {
        ReloadStartEvent().callEvent()
        asyncTask {
            var time = System.currentTimeMillis()
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