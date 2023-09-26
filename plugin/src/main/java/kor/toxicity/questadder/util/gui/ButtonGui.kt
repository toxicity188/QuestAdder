package kor.toxicity.questadder.util.gui

import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.GuiExecutor
import kor.toxicity.questadder.api.gui.GuiHolder
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.ItemWriter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ButtonGui<T: Any>(
    private val size: Int,
    private val name: ComponentReader<T>,
    private val items: Map<Int,ItemWriter<T>>
) {
    constructor(section: ConfigurationSection): this(
        section.findInt(0,"Size","size"),
        section.findString("Name","name")?.let {
            ComponentReader<T>(it)
        } ?: throw RuntimeException("name doesn't exist."),
        section.findConfig("items","Items")?.run {
            HashMap<Int,ItemWriter<T>>().apply {
                getKeys(false).forEach {
                    getConfigurationSection(it)?.let { config ->
                        put(it.toInt(),ItemWriter(config))
                    }
                }
            }
        } ?: emptyMap()
    )

    fun create(player: Player) = GuiBluePrint(player)

    inner class GuiBluePrint(val player: Player) {

        private val buttonMap = HashMap<Int,GuiItem>()
        var initializer: ((Inventory) -> Unit)? = null
        var exceptAction: ((GuiData, ItemStack, Int, MouseButton) -> Unit)? = null

        fun addButton(int: Int, writer: (T) -> ItemStack, action: (GuiData, MouseButton) -> Unit) {
            buttonMap[int] = GuiItem(writer,action)
        }
        fun addButton(int: Int, writer: ItemWriter<T>, action: (GuiData, MouseButton) -> Unit) {
            buttonMap[int] = GuiItem({
                writer.write(it)
            },action)
        }

        fun open(player: Player, t: T): GuiHolder {
            return createInventory(name.createComponent(t) ?: "error!".asComponent(),size,HashMap<Int, ItemStack>().apply {
                items.forEach {
                    if (it.key/9 < this@ButtonGui.size) put(it.key, it.value.write(t))
                }
                buttonMap.forEach {
                    if (it.key/9 < this@ButtonGui.size) put(it.key, it.value.writer(t))
                }
            }).open(player, object : GuiExecutor {
                override fun end(data: GuiData) {

                }

                override fun initialize(data: GuiData) {
                    initializer?.invoke(data.inventory)
                }

                override fun click(
                    data: GuiData,
                    clickedItem: ItemStack,
                    clickedSlot: Int,
                    isPlayerInventory: Boolean,
                    button: MouseButton
                ) {
                    if (isPlayerInventory) return
                    buttonMap[clickedSlot]?.let {
                        it.action(data,button)
                        buttonMap.forEach { e ->
                            data.inventory.setItem(e.key,e.value.writer(t))
                        }
                    } ?: exceptAction?.invoke(data,clickedItem,clickedSlot,button)
                    player.updateInventory()
                }
            })
        }
    }

    private inner class GuiItem(val writer: (T) -> ItemStack, val action: (GuiData, MouseButton) -> Unit)
}