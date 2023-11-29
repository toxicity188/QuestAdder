package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.getAsStringList
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.ItemWriter
import org.bukkit.Material
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.entity.Player

object ShopManager: QuestAdderManager {

    var loreBuy = listOf<ComponentReader<Player>>(
        ComponentReader(""),
        ComponentReader("<color:gray>(buy - left click)")
    )
        private set
    var loreSell = listOf<ComponentReader<Player>>(
        ComponentReader(""),
        ComponentReader("<color:gray>(sell - right click)")
    )
        private set
    var loreBuyAndSell = listOf<ComponentReader<Player>>(
        ComponentReader(""),
        ComponentReader("<color:gray>(buy - left click)"),
        ComponentReader("<color:gray>(sell - right click)")
    )
        private set


    var loreBuyPrice = listOf<ComponentReader<Player>>(
        ComponentReader("Buy price: \$price")
    )
        private set
    var loreSellPrice = listOf<ComponentReader<Player>>(
        ComponentReader("Sell price: \$price")
    )
        private set
    var loreRemainStock = listOf<ComponentReader<Player>>(
        ComponentReader("Remain stock: \$stock")
    )
        private set


    var messageBuyCannotBuying = ComponentReader<Player>("<color:red>Sorry, this item cannot buying.")
        private set
    var messageBuyNotHaveMoney = ComponentReader<Player>("<color:red>Sorry, you don't have enough money to buy.")
        private set
    var messageBuyNotHaveStorage = ComponentReader<Player>("<color:red>Sorry, you don't have enough storage to buy.")
        private set
    var messageBuyNotHaveItem = ComponentReader<Player>("<color:red>Sorry, you don't have enough ingredient to buy.")
        private set
    var messageBuyNotHaveStock = ComponentReader<Player>("<color:red>Sorry, this item is sold out.")
        private set

    var messageSellCannotSelling = ComponentReader<Player>("<color:red>Sorry, this item cannot selling.")
        private set
    var messageSellNotHaveItem = ComponentReader<Player>("<color:red>Sorry, you don't have item to sell.")
        private set
    var messageSellNotHaveStorage = ComponentReader<Player>("<color:red>Sorry, you don't have enough storage to sell.")
        private set

    private val pageBeforeConfig = MemoryConfiguration().apply {
        set("display", "Go to before page")
        set("type", Material.STONE_BUTTON.toString())
        set("lore", arrayOf(
            "<color:gray>Go to before page.",
            "<color:gray>Current page: \$page"
        ))
    }
    private val pageAfterConfig = MemoryConfiguration().apply {
        set("display", "Go to after page")
        set("type", Material.STONE_BUTTON.toString())
        set("lore", arrayOf(
            "<color:gray>Go to after page.",
            "<color:gray>Current page: \$page"
        ))
    }
    private val playerStatusConfig = MemoryConfiguration().apply {
        set("display", "Player status")
        set("type", Material.PLAYER_HEAD.toString())
        set("lore", arrayOf(
            "<color:gray>current money: \$money",
        ))
    }


    var pageBefore = ItemWriter<Player>(pageBeforeConfig)
        private set
    var pageAfter = ItemWriter<Player>(pageAfterConfig)
        private set
    var playerStatus = ItemWriter<Player>(playerStatusConfig)
        private set

    override fun start(adder: QuestAdderBukkit) {
    }

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
        checker(0.0, "initializing shop config...")
        adder.loadFile("shop")?.let {
            it.getAsStringList("lore-buy")?.let { lore ->
                loreBuy = lore.map { s ->
                    ComponentReader(s)
                }
            }
            it.getAsStringList("lore-sell")?.let { lore ->
                loreSell = lore.map { s ->
                    ComponentReader(s)
                }
            }
            it.getAsStringList("lore-buy-and-sell")?.let { lore ->
                loreBuyAndSell = lore.map { s ->
                    ComponentReader(s)
                }
            }
            it.getAsStringList("lore-buy-price")?.let { lore ->
                loreBuyPrice = lore.map { s ->
                    ComponentReader(s)
                }
            }
            it.getAsStringList("lore-sell-price")?.let { lore ->
                loreSellPrice = lore.map { s ->
                    ComponentReader(s)
                }
            }
            it.getAsStringList("lore-remain-stock")?.let { lore ->
                loreRemainStock = lore.map { s ->
                    ComponentReader(s)
                }
            }
            it.getString("message-buy-cannot-buying")?.let { s ->
                messageBuyCannotBuying = ComponentReader(s)
            }
            it.getString("message-buy-not-have-money")?.let { s ->
                messageBuyNotHaveMoney = ComponentReader(s)
            }
            it.getString("message-buy-not-have-storage")?.let { s ->
                messageBuyNotHaveStorage = ComponentReader(s)
            }
            it.getString("message-buy-not-have-item")?.let { s ->
                messageBuyNotHaveItem = ComponentReader(s)
            }
            it.getString("message-buy-not-have-stock")?.let { s ->
                messageBuyNotHaveStock = ComponentReader(s)
            }
            it.getString("message-sell-cannot-selling")?.let { s ->
                messageSellCannotSelling = ComponentReader(s)
            }
            it.getString("message-sell-not-have-item")?.let { s ->
                messageSellNotHaveItem = ComponentReader(s)
            }
            it.getString("message-sell-not-have-storage")?.let { s ->
                messageSellNotHaveStorage = ComponentReader(s)
            }

            pageBefore = ItemWriter(it.getConfigurationSection("button-page-before") ?: pageBeforeConfig)
            pageAfter = ItemWriter(it.getConfigurationSection("button-page-after") ?: pageAfterConfig)
            playerStatus = ItemWriter(it.getConfigurationSection("button-player-status") ?: playerStatusConfig)
        }
        checker(1.0, "finalizing shop config...")
    }

    override fun end(adder: QuestAdderBukkit) {
    }
}
