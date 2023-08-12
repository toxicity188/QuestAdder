package kor.toxicity.questadder.extension

import kor.toxicity.questadder.util.gui.Gui
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

fun createInventory(name: Component, size: Int, map: Map<Int,ItemStack> = emptyMap()) = Gui(size,name,map)