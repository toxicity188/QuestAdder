package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdder
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun CommandSender.send(message: String) = send(message.colored())
fun CommandSender.send(message: Component) = sendMessage(QuestAdder.Prefix.plugin.append(message))
fun CommandSender.info(message: String) = info(message.colored())
fun CommandSender.info(message: Component) = sendMessage(QuestAdder.Prefix.info.append(message))
fun CommandSender.warn(message: String) = warn(message.colored())
fun CommandSender.warn(message: Component) = sendMessage(QuestAdder.Prefix.warn.append(message))

fun Player.give(vararg itemStack: ItemStack) = inventory.addItem(*itemStack)