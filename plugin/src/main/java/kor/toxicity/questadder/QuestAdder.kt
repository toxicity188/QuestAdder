package kor.toxicity.questadder

import com.ticxo.playeranimator.PlayerAnimatorImpl
import com.ticxo.playeranimator.api.PlayerAnimator
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.event.ReloadEndEvent
import kor.toxicity.questadder.event.ReloadStartEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

class QuestAdder: JavaPlugin() {
    companion object {
        lateinit var animator: PlayerAnimator
            private set
        private lateinit var plugin: QuestAdder

        fun send(message: String) = plugin.logger.info(message)
        fun warn(message: String) = plugin.logger.warning(message)

        fun task(action: () -> Unit) = Bukkit.getScheduler().runTask(plugin,action)
        fun asyncTask(action: () -> Unit) = Bukkit.getScheduler().runTaskAsynchronously(plugin,action)
        fun taskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLater(plugin,action,delay)
        fun asyncTaskLater(delay: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,action,delay)
        fun taskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimer(plugin,action,delay,period)
        fun asyncTaskTimer(delay: Long, period: Long, action: () -> Unit) = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,action,delay,period)

        fun reload(callback: (Long) -> Unit) = plugin.reload(callback)

        private val managerList = mutableListOf(
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
        internal fun reload(section: ConfigurationSection) {
            defaultTypingSpeed = section.getLong("default-typing-speed",1L)
        }
    }
    object Prefix {

        var plugin: Component = "[QuestAdder] ".asComponent(GOLD).append(Component.empty().color(WHITE))
            private set
        var info: Component = " [!] ".asComponent(GOLD).append(Component.empty().color(WHITE))
            private set
        var warn: Component = " [!] ".asComponent(RED).append(Component.empty().color(WHITE))
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
        } catch (ex: Exception) {
            warn("unsupported version found!.")
            warn("plugin will be disabled.")
            Bukkit.getPluginManager().disablePlugin(this)
        }
        getCommand("questadder")?.setExecutor(command.createTabExecutor())
        managerList.forEach {
            it.start(this)
        }
        load()
        send("plugin enabled.")
    }

    override fun onDisable() {
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
}