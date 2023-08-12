package kor.toxicity.questadder.util.gui.player

import kor.toxicity.questadder.extension.GRAY
import kor.toxicity.questadder.extension.WHITE
import kor.toxicity.questadder.extension.asClearComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack

enum class PlayerGuiButtonType {
    PAGE_BEFORE {
        override fun applyItem(data: PlayerGuiData, itemStack: ItemStack): ItemStack {
            return itemStack
        }
    },
    PAGE_AFTER {
        override fun applyItem(data: PlayerGuiData, itemStack: ItemStack): ItemStack {
            return itemStack
        }
    },
    TYPE_SORT {
        override fun applyItem(data: PlayerGuiData, itemStack: ItemStack): ItemStack {
            val split = " / ".asClearComponent().color(GRAY)
            return itemStack.apply {
                itemMeta = itemMeta?.apply {
                    lore(ArrayList<Component>().apply {
                        var comp = Component.empty()
                        val typeSet = data.typeSet
                        typeSet.forEachIndexed { index, s ->
                            comp = comp.append(if (data.selectedType == s) s.asClearComponent().color(WHITE).decorate(TextDecoration.BOLD) else s.asClearComponent().color(
                                GRAY))
                            if (index < typeSet.lastIndex) comp = comp.append(split)
                        }
                        add(comp)
                        lore()?.let {
                            add(Component.empty())
                            addAll(it)
                        }
                    })
                }
            }
        }
    }
    ;
    abstract fun applyItem(data: PlayerGuiData, itemStack: ItemStack): ItemStack
}