package kor.toxicity.questadder.util.gui

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class Gui(size: Int, val name: Component, val items: Map<Int,ItemStack>) {

    constructor(size: Int, section: ConfigurationSection): this(
        size,
        section.findString("name","Name")?.colored() ?: throw RuntimeException("gui name doesn't exist."),
        HashMap<Int, ItemStack>().apply {
            section.findConfig("Items","items")?.run {
                getKeys(false).forEach {
                    getAsItemStack(it)?.let { itemStack ->
                        put(it.toInt(),itemStack)
                        Unit
                    } ?: throw RuntimeException("the key \"$it\" is not an item.")
                }
            }
        }
    )

    val size = size.coerceAtLeast(1).coerceAtMost(6) * 9

    inner class GuiHolder(player: Player, val executor: GuiExecutor): InventoryHolder {
        private val inv = Bukkit.createInventory(this,size,name).apply {
            for (item in items) {
                setItem(item.key,item.value)
            }
        }
        val data = GuiData(this@Gui,inv,executor,player)
        init {
            executor.initialize(data)
        }
        override fun getInventory(): Inventory = inv
    }

    fun open(player: Player, executor: GuiExecutor): Gui.GuiHolder {
        val beforeHolder = player.openInventory.topInventory.holder
        return if (beforeHolder is SubExecutor) {
            beforeHolder.safeEnd = true
            val holder = GuiHolder(player,object : GuiExecutor {
                override fun end(data: GuiData) {
                    beforeHolder.safeEnd = false
                    executor.end(data)
                }

                override fun initialize(data: GuiData) {
                    executor.initialize(data)
                }

                override fun click(
                    data: GuiData,
                    clickedItem: ItemStack,
                    clickedSlot: Int,
                    isPlayerInventory: Boolean,
                    button: MouseButton
                ) {
                    executor.click(data, clickedItem, clickedSlot, isPlayerInventory, button)
                }
            })
            player.openInventory(holder.inventory)
            holder
        } else {
            val holder = GuiHolder(player,executor)
            player.openInventory(holder.inventory)
            holder
        }
    }
}