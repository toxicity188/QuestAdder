package kor.toxicity.questadder.command

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

interface CommandAPIExecutor {
    fun name(): String
    fun aliases(): Array<String>
    fun description(): Component
    fun usage(): Component
    fun opOnly(): Boolean
    fun allowedSender(): Array<SenderType>
    fun permissions(): Array<String>
    fun length(): Int
    fun execute(api: CommandAPI, sender: CommandSender, args: Array<String>)
    fun tabComplete(api: CommandAPI, sender: CommandSender, args: Array<String>): List<String>?
}
