package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.api.event.DialogStartEvent
import kor.toxicity.questadder.api.event.QuestAdderPlayerEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.mechanic.npc.ActualNPC
import kor.toxicity.questadder.util.ItemWriter
import kor.toxicity.questadder.util.gui.GuiData
import kor.toxicity.questadder.util.gui.GuiExecutor
import kor.toxicity.questadder.util.gui.MouseButton
import net.kyori.adventure.text.Component
import org.bukkit.Material
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

    fun open(player: Player, event: DialogStartEvent, guiName: Component, talker: Component, talk: Component?) {

        createInventory(name ?: guiName,size,HashMap<Int, ItemStack>().apply {
            if (talk != null) put(center, ItemStack(Material.ENCHANTED_BOOK).apply {
                itemMeta = itemMeta?.apply {
                    displayName(talker.append(":".asClearComponent()))
                    lore(listOf(talk))
                }
            })
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
                qnaItemMap[clickedSlot]?.dialogs?.random()?.start(player,event.npc as ActualNPC)
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