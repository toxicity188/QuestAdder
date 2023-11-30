package kor.toxicity.questadder.command

import kor.toxicity.questadder.extension.asClearComponent
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.extension.warn
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.util.TreeMap

class CommandAPI(private val name: String) {
    private val map = TreeMap<String, CommandAPIExecutor>()

    fun addCommand(name: String, apply: Builder.() -> Unit): CommandAPI {
        map[name] = Builder(name).apply(apply).build()
        return this
    }
    fun addApiCommand(name: String, apply: ApiBuilder.() -> Unit, commandApiApply: CommandAPI.() -> Unit): CommandAPI {
        ApiBuilder(name).apply(apply).build().apply(commandApiApply)
        return this
    }

    init {
        addCommand("help") {
            aliases = arrayOf("h", "도움말")
            description = "show the list of registered command.".asComponent()
            usage = "help ".asComponent().append("[page]".asComponent().color(NamedTextColor.DARK_AQUA))
            permissions = arrayOf("${name.replace(' ','.')}.help")
            executor = { _, sender, args ->
                val page = (if (args.isNotEmpty()) try {
                    (args[0].toInt() - 1)
                } catch (ex: Exception) {
                    0
                } else 0).coerceAtMost((map.size - 1) / 8).coerceAtLeast(0)
                sender.send("----------< ${page + 1} / ${(map.size - 1) / 8 + 1} >----------".asComponent(NamedTextColor.GOLD))
                sender.send(Component.empty())
                sender.send(Component.empty()
                    .append("     <argument>".asComponent(NamedTextColor.AQUA))
                    .append(" - Required, ".asComponent())
                    .append("[argument]".asComponent(NamedTextColor.DARK_AQUA))
                    .append(" - Optional".asComponent())
                )
                sender.send(Component.empty())
                map.values.toList().subList(page * 8, ((page + 1) * 8).coerceAtMost(map.size)).forEach {
                    sender.send(Component.empty()
                        .append("/$name ".asClearComponent().style(Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD)))
                        .append(it.usage())
                        .append(" (${it.aliases().joinToString(",")}) - ".asComponent().color(NamedTextColor.GRAY))
                        .append(it.description())
                    )
                }
                sender.send(Component.empty())
                sender.send("------------------------------".asComponent(NamedTextColor.GOLD))
            }
            tabCompleter = { _, _, args ->
                if (args.size == 1) (1..((map.size - 1) / 8 + 1).coerceAtLeast(1)).map {
                    it.toString()
                }.filter {
                    it.contains(args[0])
                } else null
            }
        }
    }

    private fun execute(sender: CommandSender, args: Array<String>) {
        var arr = args
        var size = args.size - 1
        val get: String
        if (args.isEmpty() || args[0].isEmpty()) {
            size += 1
            arr = arrayOf("help")
            get = "help"
        } else get = args[0]
        getExecutor(sender, get, size, true)?.execute(this, sender, arr.copyOfRange(1, arr.size))
    }
    private fun tabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        return if (args.size <= 1) map.keys.filter {
            it.contains(args[0])
        } else getExecutor(sender, args[0], args.size - 1, false)?.tabComplete(this, sender, args.copyOfRange(1, args.size))
    }

    fun createTabExecutor() = object : TabExecutor {
        override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<String>
        ): List<String>? {
            return tabComplete(sender, args)
        }

        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<String>
        ): Boolean {
            execute(sender, args)
            return true
        }

    }

    private fun getExecutor(sender: CommandSender, select: String, length: Int, info: Boolean, checkLength: Boolean = true): CommandAPIExecutor? {
        val executor = map[select] ?: map.values.firstOrNull {
            it.aliases().contains(select)
        } ?: run {
            if (info) sender.warn("unknown command: $select")
            return null
        }
        if (executor.opOnly() && !sender.isOp) {
            if (info) sender.warn("you are not op!")
            return null
        }
        if (executor.allowedSender().none {
            it.accept(sender::class.java)
        }) {
            if (info) sender.warn("allowed sender: ${executor.allowedSender().joinToString(", ") { 
                it.name.lowercase()
            }}")
            return null
        }
        val permission = executor.permissions()
        if (permission.isNotEmpty() && permission.none {
            sender.hasPermission(it)
        }) {
            if (info) sender.warn("you have to get this permission to execute this command: ${permission.joinToString(", ")}")
            return null
        }
        if (checkLength && length < executor.length()) {
            if (info) sender.warn("usage: ".asClearComponent().append("/$name ".asComponent().style(Style.style(NamedTextColor.YELLOW))).append(executor.usage()))
            return null
        }
        return executor
    }

    inner class ApiBuilder(private val subName: String) {
        private val innerApi = CommandAPI("$name $subName")
        var aliases = emptyArray<String>()
        var description: Component = Component.text("$subName-related command.")
        var usage: Component = Component.text(subName)
        var opOnly = false
        var allowedSender = SenderType.entries.toTypedArray()
        var length = 0
        var permissions = emptyArray<String>()
        fun build(): CommandAPI {
            map[subName] = object : CommandAPIExecutor {
                override fun name(): String {
                    return subName
                }

                override fun aliases(): Array<String> {
                    return aliases
                }

                override fun description(): Component {
                    return description
                }

                override fun usage(): Component {
                    return usage
                }

                override fun opOnly(): Boolean {
                    return opOnly
                }

                override fun allowedSender(): Array<SenderType> {
                    return allowedSender
                }

                override fun permissions(): Array<String> {
                    return permissions
                }

                override fun length(): Int {
                    return length
                }

                override fun execute(api: CommandAPI, sender: CommandSender, args: Array<String>) {
                    innerApi.execute(sender, args)
                }

                override fun tabComplete(
                    api: CommandAPI,
                    sender: CommandSender,
                    args: Array<String>
                ): List<String>? {
                    return innerApi.tabComplete(sender, args)
                }
            }
            return innerApi
        }
    }
    class Builder(private val name: String) {
        var aliases = emptyArray<String>()
        var description: Component = Component.empty()
        var usage: Component = Component.empty()
        var opOnly = false
        var allowedSender = SenderType.entries.toTypedArray()
        var length = 0
        var permissions = emptyArray<String>()
        var executor: (CommandAPI, CommandSender, Array<String>) -> Unit = { _, _, _ ->
        }
        var tabCompleter: (CommandAPI, CommandSender, Array<String>) -> List<String>? = { _, _, _ ->
            null
        }
        fun build(): CommandAPIExecutor {
            return object : CommandAPIExecutor {
                override fun name(): String {
                    return name
                }
                override fun aliases(): Array<String> {
                    return aliases
                }

                override fun description(): Component {
                    return description
                }

                override fun usage(): Component {
                    return usage
                }

                override fun opOnly(): Boolean {
                    return opOnly
                }

                override fun allowedSender(): Array<SenderType> {
                    return allowedSender
                }

                override fun length(): Int {
                    return length
                }

                override fun permissions(): Array<String> {
                    return permissions
                }

                override fun execute(api: CommandAPI, sender: CommandSender, args: Array<String>) {
                    executor(api, sender, args)
                }

                override fun tabComplete(
                    api: CommandAPI,
                    sender: CommandSender,
                    args: Array<String>
                ): List<String>? {
                    return tabCompleter(api, sender, args)
                }

            }
        }
    }
}
