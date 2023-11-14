package kor.toxicity.questadder.command

import kor.toxicity.questadder.extension.send
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.lang.StringBuilder


class CommandAPI(prefix: String) {
    private val commandMap = HashMap<String,CommandData>()
    init {
        create("help").apply {
            aliases = arrayOf("h","도움말")
            description = "help command."
            usage = "help"
            opOnly = false
            executor = { sender,_ ->
               commandMap.values.forEach {
                   if (!it.opOnly || sender.isOp) sender.send("/$prefix ${it.usage} ${
                       if (it.aliases.isNotEmpty()) StringBuilder().append('(').apply {
                           it.aliases.forEachIndexed { index, s ->
                               append(s)
                               if (index < it.aliases.size - 1) append(",")
                           }
                       }.append(')') else ""
                   }: ${it.description}")
               }
            }
        }.done()
    }


    private fun create(tag: String) = CommandBuilder(tag)

    fun execute(command: String, sender: CommandSender, args: Array<String>) {
        (commandMap[command] ?: commandMap.values.firstOrNull { it.aliases.contains(command) })?.run {
            if (opOnly && !sender.isOp) return sender.send("sorry, this command is op-only command.")
            if (length > args.size - 1) return sender.send("this command requires at least $length of arguments.")
            if (allowedSender.none { it.accept(sender.javaClass) }) return sender.send("applicable sender list: ${
                StringBuilder().apply {
                    allowedSender.forEachIndexed { index, senderType ->
                        append(senderType.display)
                        if (index < allowedSender.size - 1) append(", ")
                    }
                }
            }")
            executor(sender,args)
        } ?: sender.send("unknown command: $command")
    }
    fun tabComplete(command: String, sender: CommandSender, args: Array<String>): List<String>? = (commandMap[command] ?: commandMap.values.firstOrNull { it.aliases.contains(command) })?.run {
        if (opOnly && !sender.isOp) return null
        tabComplete(sender,args)
    }
    private fun getCommandList(sender: CommandSender) = commandMap.entries.filter {
        !it.value.opOnly || sender.isOp
    }.map {
        it.key
    }
    fun searchCommand(prefix: String, sender: CommandSender): List<String> = ArrayList(getCommandList(sender)).apply {
        commandMap.values.forEach {
            if (!it.opOnly || sender.isOp) addAll(it.aliases)
        }
    }.filter {
        it.startsWith(prefix)
    }

    fun addCommand(name: String, apply: CommandAPI.CommandBuilder.() -> Unit) = create(name).apply(apply).done()
    fun addCommandAPI(name: String, commandAliases: Array<String>, commandDescription: String, commandOpOnly: Boolean, api: CommandAPI): CommandAPI {
        addCommand(name) {
            aliases = commandAliases
            description = commandDescription
            usage = name
            opOnly = commandOpOnly
            executor = { commandSender, strings ->
                api.execute(if (strings.size > 1) strings[1] else "help",commandSender,if (strings.size > 1) strings.toMutableList().apply {
                    removeAt(0)
                }.toTypedArray() else strings)
            }
            tabComplete = { sender, args ->
                if (args.size == 2) api.searchCommand(args[1],sender) else api.tabComplete(args[1],sender,args.toMutableList().apply {
                    removeAt(0)
                }.toTypedArray())
            }
        }
        return this
    }
    fun createTabExecutor() = object : TabExecutor {
        override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<String>
        ): List<String>? {
            return if (args.size == 1) searchCommand(args[0],sender) else tabComplete(args[0],sender,args)
        }

        override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<String>
        ): Boolean {
            (if (args.isEmpty()) arrayOf("help") else args).run {
                execute(get(0),sender,this)
            }
            return true
        }
    }

    inner class CommandBuilder(private val tag: String) {
        var aliases: Array<String> = emptyArray()
        var length = 0
        var description = "unknown description."
        var usage = "unknown usage"
        var opOnly = true
        var allowedSender: Array<SenderType> = arrayOf(SenderType.CONSOLE,SenderType.PLAYER)
        var executor: (CommandSender,Array<String>) -> Unit = { _,_ ->
        }
        var tabComplete: (CommandSender,Array<String>) -> List<String>? = { _,_ ->
            null
        }

        fun done(): CommandAPI {
            commandMap[tag] = CommandData(
                length = length,
                aliases = aliases,
                description = description,
                usage = usage,
                opOnly = opOnly,
                allowedSender = allowedSender,
                executor = executor,
                tabComplete = tabComplete,
            )
            return this@CommandAPI
        }
    }
    private class CommandData(
        val aliases: Array<String>,
        val description: String,
        val length: Int,
        val usage: String,
        val opOnly: Boolean,
        val allowedSender: Array<SenderType>,
        val executor: (CommandSender,Array<String>) -> Unit,
        val tabComplete:  (CommandSender,Array<String>) -> List<String>?
    )
}
