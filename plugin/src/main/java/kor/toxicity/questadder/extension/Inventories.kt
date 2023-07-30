package kor.toxicity.questadder.extension

import kor.toxicity.questadder.util.GuiWrapper
import net.kyori.adventure.text.Component

fun createInventory(name: Component, size: Int) = GuiWrapper(name,size)