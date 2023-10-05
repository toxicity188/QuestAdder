package kor.toxicity.questadder.util.gui

import kor.toxicity.questadder.api.gui.*
import kor.toxicity.questadder.extension.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class Gui(private val rawSize: Int, val name: Component, private val items: Map<Int,ItemStack>): IGui {

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

    val size = rawSize.coerceAtLeast(1).coerceAtMost(6) * 9

    override fun copy(): IGui {
        return Gui(rawSize, name, items)
    }

    override fun setName(component: Component): IGui {
        return Gui(rawSize, component, items)
    }

    override fun open(player: Player, executor: GuiExecutor): GuiHolder {
        val beforeHolder = player.openInventory.topInventory.holder
        return if (beforeHolder is SubExecutor) {
            beforeHolder.safeEnd = true
            val holder = GuiHolder(this,size,name,player,object : GuiExecutor {
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
            },items)
            player.openInventory(holder.inventory)
            holder
        } else {
            val holder = GuiHolder(this,size,name,player,executor, items)
            player.openInventory(holder.inventory)
            holder
        }
    }

    override fun getGuiName(): Component {
        return name
    }

    override fun getInnerItems(): Map<Int, ItemStack> {
        return items
    }
}
