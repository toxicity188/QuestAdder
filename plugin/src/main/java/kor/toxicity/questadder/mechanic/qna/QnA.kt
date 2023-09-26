package kor.toxicity.questadder.mechanic.qna

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.DialogStartEvent
import kor.toxicity.questadder.api.event.QuestAdderPlayerEvent
import kor.toxicity.questadder.api.gui.GuiData
import kor.toxicity.questadder.api.gui.GuiExecutor
import kor.toxicity.questadder.api.gui.MouseButton
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.mechanic.dialog.Dialog
import kor.toxicity.questadder.mechanic.dialog.DialogEndData
import kor.toxicity.questadder.util.ItemWriter
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class QnA(adder: QuestAdderBukkit, file: File, key: String, section: ConfigurationSection) {
    private val name = section.findString("name","Name")?.colored()
    private val size = section.findInt(3,"size","Size")
    private val center = size / 2 * 9 + 4
    private val qnaItemMap = HashMap<Int, QnAItem>().apply {
        adder.addLazyTask {
            section.findConfig("items")?.run {
                getKeys(false).forEach {
                    getConfigurationSection(it)?.let { config ->
                        try {
                            val i = it.toInt()
                            if (i/9 < this@QnA.size) {
                                put(i, QnAItem(config))
                            }
                            Unit
                        } catch (ex: Exception) {
                            QuestAdderBukkit.warn("unable to load the qna item: $it ($key in ${file.name})")
                            QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                        }
                    } ?: QuestAdderBukkit.warn("syntax error: the key \"$it\" is not a configuration section. ($key in ${file.name})")
                }
            }
        }
    }

    fun open(data: DialogEndData, startFunction: (Dialog) -> Unit) {

        createInventory(name ?: data.guiName,size,HashMap<Int, ItemStack>().apply {
            if (data.talk != null) put(center, ItemStack(Material.ENCHANTED_BOOK).apply {
                itemMeta = itemMeta?.apply {
                    displayName(data.talker.append(":".asClearComponent()))
                    lore(listOf(data.talk))
                }
            })
            qnaItemMap.forEach {
                put(it.key,it.value.item.write(data.event))
            }
        }).open(data.player, object : GuiExecutor {
            override fun end(data: GuiData) {

            }

            override fun initialize(data: GuiData) {
            }

            override fun click(
                data: GuiData,
                clickedItem: ItemStack,
                clickedSlot: Int,
                isPlayerInventory: Boolean,
                button: MouseButton
            ) {
                if (isPlayerInventory) return
                qnaItemMap[clickedSlot]?.dialogs?.random()?.let(startFunction)
            }
        })
    }
    private class QnAItem(section: ConfigurationSection) {
        val item = section.findConfig("item","Item")?.let {
            ItemWriter<QuestAdderPlayerEvent>(it)
        } ?: throw RuntimeException("item doesn't exist.")
        val dialogs = (section.findStringList("Dialog","dialog","dialogs","Dialogs")?.mapNotNull {
            DialogManager.getDialog(it)
        } ?: throw RuntimeException("dialog doesn't exist.")).apply {
            if (isEmpty()) throw RuntimeException("dialog is empty.")
        }
    }
}