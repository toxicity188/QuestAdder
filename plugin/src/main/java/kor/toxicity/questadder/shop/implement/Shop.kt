package kor.toxicity.questadder.shop.implement

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.GuiExecutor
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.api.mechanic.DialogSender
import kor.toxicity.questadder.api.shop.IShop
import kor.toxicity.questadder.extension.applyComma
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.createInventory
import kor.toxicity.questadder.extension.getMoney
import kor.toxicity.questadder.manager.ShopManager
import kor.toxicity.questadder.shop.blueprint.ShopBlueprint
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.text.DecimalFormat

class Shop(private val blueprint: ShopBlueprint, jsonObject: JsonObject): IShop {
    companion object {
        private val emptyArray = JsonArray()
        private val emptyObject = JsonObject()
        private val format = DecimalFormat("#,###")
    }

    private val shopPage: List<ShopPage> = try {
        ArrayList<ShopPage>().apply {
            val pageObject = jsonObject.getAsJsonArray("pages") ?: emptyArray
            blueprint.pages.forEachIndexed { index, shopPageBlueprint ->
                add(ShopPage(shopPageBlueprint, pageObject.get(index).asJsonObject ?: emptyObject))
            }
        }
    } catch (ex: Exception) {
        blueprint.pages.map {
            ShopPage(it, emptyObject)
        }
    }

    fun serialize() = JsonObject().apply {
        add("pages", JsonArray().apply {
            shopPage.forEach {
                add(it.serialize())
            }
        })
    }

    fun cancel() {
        shopPage.forEach {
            it.cancel()
        }
    }

    override fun open(player: Player, sender: DialogSender) {
        createInventory(blueprint.name.createComponent(player) ?: Component.text("error"), blueprint.size).open(player, object : GuiExecutor {

            private var currentShopPage = shopPage[0]
            private var currentShopIndex = 0

            override fun initialize(data: GuiData) {
                val inv = data.inventory
                val size = blueprint.size * 9
                for (i in 0 until size) {
                    inv.setItem(i, null)
                }
                currentShopPage.shopItem.forEach {
                    if (it.key >= size - 9) return@forEach
                    inv.setItem(it.key,it.value.blueprint.builder().apply {
                        itemMeta = itemMeta?.apply {
                            QuestAdderBukkit.platform.setLore(this, QuestAdderBukkit.platform.getLore(this).toMutableList().apply {

                                val buyPriceInstance = it.value.blueprint.buyPrice
                                val sellPriceInstance = it.value.blueprint.sellPrice


                                val buyPrice = buyPriceInstance.equation.buyEvaluate(it.value, player)
                                val sellPrice = sellPriceInstance.equation.sellEvaluate(it.value, player)

                                val stock = it.value.getStock(player)

                                val canBuy = buyPriceInstance.price > 0 || buyPriceInstance.item.isNotEmpty()
                                val canSell = sellPriceInstance.price > 0 || sellPriceInstance.item.isNotEmpty()

                                if (canBuy) addAll(ShopManager.loreBuyPrice.mapNotNull { reader ->
                                    reader.createComponent(player, mapOf("\$price" to listOf(Component.text(format.format(buyPrice)))))
                                })
                                if (canSell) addAll(ShopManager.loreSellPrice.mapNotNull { reader ->
                                    reader.createComponent(player, mapOf("\$price" to listOf(Component.text(format.format(sellPrice)))))
                                })
                                if (it.value.blueprint.stock > 0) addAll(ShopManager.loreRemainStock.mapNotNull { reader ->
                                    reader.createComponent(player, mapOf("\$stock" to listOf(Component.text(format.format(stock)))))
                                })

                                if (canBuy && canSell) {
                                    addAll(ShopManager.loreBuyAndSell.mapNotNull { reader ->
                                        reader.createComponent(player)
                                    })
                                }
                                else if (canBuy) {
                                    addAll(ShopManager.loreBuy.mapNotNull { reader ->
                                        reader.createComponent(player)
                                    })
                                }
                                else if (canSell) {
                                    addAll(ShopManager.loreSell.mapNotNull { reader ->
                                        reader.createComponent(player)
                                    })
                                }
                            })
                        }
                    })
                    inv.setItem(size - 3, ShopManager.pageAfter.write(player, mapOf(
                        "\$page" to listOf(Component.text(currentShopIndex + 1)),
                        "\$max-page" to listOf(Component.text(shopPage.size))
                    )))
                    inv.setItem(size - 5, ShopManager.playerStatus.write(player, mapOf(
                        "\$page" to listOf(Component.text(currentShopIndex)),
                        "\$money" to listOf(player.getMoney().applyComma().asComponent())
                    )).apply {
                        itemMeta = itemMeta?.apply {
                            if (this is SkullMeta) owningPlayer = player
                        }
                    })
                    inv.setItem(size - 7, ShopManager.pageBefore.write(player, mapOf(
                        "\$page" to listOf(Component.text(currentShopIndex + 1)),
                        "\$max-page" to listOf(Component.text(shopPage.size))
                    )))
                }
            }

            override fun click(
                data: GuiData,
                clickedItem: ItemStack,
                clickedSlot: Int,
                isPlayerInventory: Boolean,
                button: MouseButton
            ) {
                if (isPlayerInventory) return
                val size = blueprint.size * 9
                if (clickedSlot > size - 9) {
                    when (clickedSlot - size) {
                        -3 -> {
                            currentShopIndex = (currentShopIndex + 1).coerceAtMost(shopPage.lastIndex)
                            currentShopPage = shopPage[currentShopIndex]
                            initialize(data)
                        }
                        -7 -> {
                            currentShopIndex = (currentShopIndex - 1).coerceAtLeast(0)
                            currentShopPage = shopPage[currentShopIndex]
                            initialize(data)
                        }
                    }
                } else {
                    currentShopPage.shopItem[clickedSlot]?.let {
                        when (button) {
                            MouseButton.LEFT -> {
                                if (it.buy(player, sender, this@Shop)) initialize(data)
                                Unit
                            }
                            MouseButton.SHIFT_LEFT -> {
                                if (it.buy(player, sender, this@Shop, 64)) initialize(data)
                                Unit
                            }
                            MouseButton.RIGHT -> {
                                if (it.sell(player, sender, this@Shop)) initialize(data)
                                Unit
                            }
                            MouseButton.SHIFT_RIGHT -> {
                                if (it.sell(player, sender, this@Shop, 64)) initialize(data)
                                Unit
                            }
                            else -> {}
                        }
                    }
                }
            }

            override fun end(data: GuiData) {
            }
        })
    }
    override fun getKey() = blueprint.id
    fun getFile() = blueprint.file
}
