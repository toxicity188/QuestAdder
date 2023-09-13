package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.api.item.JsonItemDatabase
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.extension.getAsItemStack
import kor.toxicity.questadder.extension.give
import kor.toxicity.questadder.extension.info
import kor.toxicity.questadder.extension.warn
import kor.toxicity.questadder.hooker.item.ItemsAdderItemDatabase
import kor.toxicity.questadder.hooker.item.MMOItemsItemDatabase
import kor.toxicity.questadder.hooker.item.OraxenItemDatabase
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.regex.Pattern

object ItemManager: QuestAdderManager {

    private var itemMap = HashMap<String,ItemStack>()
    private var itemDatabaseList = ArrayList<ItemDatabase>()

    private val itemPattern = Pattern.compile("^(?<name>(([a-zA-Z]|[가-힣]|_|-|:)+))(?<argument>\\{[\\w|\\W]*})?$")

    fun getItem(name: String): ItemStack? {
        val matcher = itemPattern.matcher(name)
        return try {
            if (!matcher.find()) itemMap[name] ?: itemDatabaseList.firstNotNullOfOrNull {
                it.getItem(name)
            } else {
                val n = matcher.group("name")
                val a = matcher.group("argument")
                return itemMap[n] ?: itemDatabaseList.firstNotNullOfOrNull {
                    when (it) {
                        is JsonItemDatabase -> it.getItem(n, a)
                        else -> it.getItem(n)
                    }
                }
            }
        } catch (ex: Exception) {
            QuestAdderBukkit.warn("unable to get this item: $name")
            QuestAdderBukkit.warn("reason: ${ex.message ?: "unknown"}")
            null
        }
    }

    override fun start(adder: QuestAdderBukkit) {
        Bukkit.getPluginManager().run {
            if (isPluginEnabled("ItemsAdder")) itemDatabaseList.add(ItemsAdderItemDatabase())
            if (isPluginEnabled("Oraxen")) itemDatabaseList.add(OraxenItemDatabase())
            if (isPluginEnabled("MMOItems")) itemDatabaseList.add(MMOItemsItemDatabase())
        }
        adder.command.addCommandAPI("item", arrayOf("i","아이템"),"item-related command.", true, CommandAPI("qa i")
            .addCommand("get") {
                aliases = arrayOf("g","지급")
                description = "get specific item."
                usage = "get <item>"
                length = 1
                allowedSender = arrayOf(SenderType.PLAYER)
                executor = { sender, args ->
                    sender as Player
                    getItem(args[1])?.let { i ->
                        sender.give(i)
                        sender.info("item successfully given.")
                    } ?: run {
                        sender.warn("item not found.")
                    }
                }
                tabComplete = { _, args ->
                    if (args.size == 2) HashSet(itemMap.keys).apply {
                        itemDatabaseList.forEach {
                            addAll(it.getKeys())
                        }
                    }.filter {
                        it.startsWith(args[1])
                    } else null
                }
            })
    }
    fun addItemDatabase(database: ItemDatabase) {
        itemDatabaseList.add(database)
    }

    override fun reload(adder: QuestAdderBukkit) {
        itemMap.clear()
        val iterator = itemDatabaseList.iterator()
        while (iterator.hasNext()) {
            try {
                iterator.next().reload()
            } catch (throwable: Throwable) {
                iterator.remove()
            }
        }
        adder.loadFolder("items") { file, config ->
            config.getKeys(false).forEach {
                config.getAsItemStack(it)?.let { i ->
                    itemMap.putIfAbsent(it,i)
                    Unit
                } ?: run {
                    QuestAdderBukkit.warn("syntax error: $it (${file.name})")
                }
            }
        }
    }

    override fun end(adder: QuestAdderBukkit) {
    }

}