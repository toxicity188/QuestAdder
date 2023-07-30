package kor.toxicity.questadder.command

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

enum class SenderType(
    val display: String,
    private val sender: Class<out CommandSender>
) {
    PLAYER("player",Player::class.java),
    CONSOLE("console",ConsoleCommandSender::class.java)
    ;
    fun accept(sender: Class<out CommandSender>) = this.sender.isAssignableFrom(sender)
}