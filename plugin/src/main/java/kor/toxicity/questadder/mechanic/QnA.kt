package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.DialogStartEvent
import kor.toxicity.questadder.event.QuestAdderPlayerEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.util.ItemWriter
import kor.toxicity.questadder.util.gui.Gui
import kor.toxicity.questadder.util.gui.GuiData
import kor.toxicity.questadder.util.gui.GuiExecutor
import kor.toxicity.questadder.util.gui.MouseButton
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class QnA(adder: QuestAdder, file: File, key: String, section: ConfigurationSection) {
    private val name = section.findString("name","Name")?.colored()
    private val size = section.findInt(3,"size","Size")
    private val center = size / 2 * 9 + 4
    private val qnaItemMap = HashMap<Int,QnAItem>().apply {
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
                            QuestAdder.warn("unable to load the qna item: $it ($key in ${file.name})")
                            QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                        }
                    } ?: QuestAdder.warn("syntax error: the key \"$it\" is not a configuration section. ($key in ${file.name})")
                }
            }
        }
    }

    fun open(player: Player, event: DialogStartEvent, holder: Gui.GuiHolder?) {

        createInventory(name ?: holder?.data?.gui?.name ?: "qna".asComponent(),size,HashMap<Int, ItemStack>().apply {
            holder?.inventory?.getItem(22)?.let {
                put(center, it)
            }
            qnaItemMap.forEach {
                put(it.key,it.value.item.write(event))
            }
        }).open(player, object : GuiExecutor {
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
                qnaItemMap[clickedSlot]?.dialogs?.random()?.start(player,event.npc)
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