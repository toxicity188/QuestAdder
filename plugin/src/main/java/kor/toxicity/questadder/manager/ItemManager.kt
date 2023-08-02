package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.extension.getAsItemStack
import kor.toxicity.questadder.extension.give
import kor.toxicity.questadder.extension.info
import kor.toxicity.questadder.extension.warn
import kor.toxicity.questadder.item.ItemDatabase
import kor.toxicity.questadder.item.ItemsAdderItemDataBase
import kor.toxicity.questadder.item.OraxenItemDataBase
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ItemManager: QuestAdderManager {

    private var itemMap = HashMap<String,ItemStack>()

    private val defaultItemDataBase = object : ItemDatabase {

        override fun getItem(name: String): ItemStack? {
            return itemMap[name]
        }
        override fun getKeys(): Collection<String> {
            return itemMap.keys
        }
        override fun reload() {
        }

        override fun requiredPlugin(): String {
            return "QuestAdder"
        }
    }
    var itemDatabase = defaultItemDataBase


    fun getItem(name: String): ItemStack? {
        return itemDatabase.getItem(name)
    }

    override fun start(adder: QuestAdder) {
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
                    if (args.size == 2) itemDatabase.getKeys().filter {
                        it.startsWith(args[1])
                    } else null
                }
            })
    }

    private enum class DataBaseType(val getter: () -> ItemDatabase) {
        DEFAULT({
            defaultItemDataBase
        }),
        ORAXEN({
            OraxenItemDataBase()
        }),
        ITEMS_ADDER({
            ItemsAdderItemDataBase()
        })
    }

    override fun reload(adder: QuestAdder) {
        adder.loadFile("item")?.let { c ->
            c.getString("using")?.let {
                itemDatabase = try {
                    DataBaseType.valueOf(it.uppercase()).getter()
                } catch (ex: Throwable) {
                    QuestAdder.warn("unable to set item database to $it.")
                    defaultItemDataBase
                }
            }
        }
        if (itemDatabase === defaultItemDataBase) {
            itemMap.clear()
            adder.loadFolder("items") { file, config ->
                config.getKeys(false).forEach {
                    config.getAsItemStack(it)?.let { i ->
                        itemMap[it] = i
                    } ?: run {
                        QuestAdder.warn("Syntax error: $it (${file.name})")
                    }
                }
            }
        }
        itemDatabase.reload()
    }

    override fun end(adder: QuestAdder) {
    }

}