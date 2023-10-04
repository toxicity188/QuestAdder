package kor.toxicity.questadder.util

import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findInt
import kor.toxicity.questadder.extension.findString
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.manager.ResourcePackManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemWriter<T: Any>(section: ConfigurationSection) {
    val type = section.findString("Type","type")?.let {
        Material.valueOf(it.uppercase())
    } ?: throw RuntimeException("type not found.")
    private val display = section.findString("display","Display","name","Name")?.let { c ->
        ComponentReader<T>(c)
    }
    private var writerData = section.findInt(0,"data","custom-model-data","Data","CustomModelData")
    private val writerLore = section.findStringList("Lore","lore")?.map {
        ComponentReader<T>(it)
    }
    init {
        section.findConfig("Icon","icon")?.let {
            it.findString("Asset","asset")?.let { s ->
                ResourcePackManager.addData(ResourcePackData(type,s) {i ->
                    writerData = i
                })
            }
        }
    }

    fun write(t: T, map: Map<String, List<Component>> = emptyMap()) = ItemStack(type).apply {
        itemMeta = itemMeta?.apply {
            displayName(display?.createComponent(t, map))
            lore(writerLore?.map {
                it.createComponent(t, map)
            })
            setCustomModelData(writerData)
            addItemFlags(*ItemFlag.entries.toTypedArray())
            isUnbreakable = true
        }
    }
}
