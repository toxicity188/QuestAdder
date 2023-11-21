package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdderBukkit
import net.kyori.adventure.text.Component
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat

fun CommandSender.send(message: String) = send(message.colored())
fun CommandSender.send(message: Component) = QuestAdderBukkit.audience.sender(this).sendMessage(QuestAdderBukkit.Prefix.plugin.append(message))
fun CommandSender.info(message: String) = info(message.colored())
fun CommandSender.info(message: Component) = QuestAdderBukkit.audience.sender(this).sendMessage(QuestAdderBukkit.Prefix.info.append(message))
fun CommandSender.warn(message: String) = warn(message.colored())
fun CommandSender.warn(message: Component) = QuestAdderBukkit.audience.sender(this).sendMessage(QuestAdderBukkit.Prefix.warn.append(message))

object Money {
    private val economy = try {
        Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
    } catch (throwable: Throwable) {
        null
    }

    internal fun getMoney(p: OfflinePlayer?): Double {
        return economy?.getBalance(p) ?: 0.0
    }

    internal fun addMoney(p: OfflinePlayer?, money: Double) {
        if (economy == null) return
        if (!economy.hasAccount(p)) economy.createPlayerAccount(p)
        economy.depositPlayer(p, money)
    }

    internal fun removeMoney(p: OfflinePlayer?, money: Double) {
        if (economy == null) return
        if (!economy.hasAccount(p)) economy.createPlayerAccount(p)
        economy.withdrawPlayer(p, money)
    }

    fun vaultLoaded() = economy != null
}

fun Number.applyComma(): String = DecimalFormat.getInstance().format(toInt())
fun OfflinePlayer.addMoney(money: Double) = Money.addMoney(this,money)
fun OfflinePlayer.getMoney() = Money.getMoney(this)
fun OfflinePlayer.removeMoney(money: Double) = Money.removeMoney(this,money)

fun Player.give(vararg items: ItemStack) {
    items.forEach {
        if (storage(it) >= it.amount) inventory.addItem(it)
        else {
            val fakeItem = QuestAdderBukkit.nms.createFakeItem(it, location)
            if (PlayerDropItemEvent(this, fakeItem.getItem()).call()) fakeItem.spawn()
        }
    }
}
fun Player.take(vararg items: ItemStack) {
    val contents: Array<ItemStack?> = inventory.contents
    for (itemStack in items) {
        var amount = itemStack.amount
        for (target in contents) {
            if (amount <= 0) break
            if (target != null && itemStack.isSimilar(target)) {
                val get = target.amount
                val minus = amount.coerceAtMost(get)
                target.amount = get - minus
                amount -= minus
            }
        }
    }
}

fun Player.emptySpace(): Int {
    val inv: Inventory = inventory
    var r = 0
    for (i in 0..35) {
        val item = inv.getItem(i)
        if (item == null || item.type == Material.AIR) r++
    }
    return r
}
fun Player.storage(target : ItemStack?): Int {
    if (target == null || target.type == Material.AIR) return emptySpace()
    val inv = inventory
    val max = target.maxStackSize
    return Array(36) { i ->
        inv.getItem(i)?.run {
            if (type == Material.AIR) max
            else if (isSimilar(target)) (max - amount).coerceAtLeast(0)
            else 0
        } ?: max
    }.sum()
}
fun Player.totalAmount(item: ItemStack): Int {
    var i = 0
    for (content in inventory.contents) {
        if (content != null && item.isSimilar(content)) i += content.amount
    }
    return i
}
