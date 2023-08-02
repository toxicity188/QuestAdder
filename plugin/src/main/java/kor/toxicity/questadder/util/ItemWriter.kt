package kor.toxicity.questadder.util

import kor.toxicity.questadder.extension.findInt
import kor.toxicity.questadder.extension.findString
import kor.toxicity.questadder.extension.findStringList
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemWriter<T: Any>(section: ConfigurationSection) {
    private val type = section.getString("type")?.let {
        Material.valueOf(it.uppercase())
    } ?: throw RuntimeException("type not found.")
    private val display = section.findString("display","Display","name","Name")?.let { c ->
        ComponentReader<T>(c)
    }
    private val writerData = section.findInt(0,"data","custom-model-data")
    private val writerLore = section.findStringList("Lore","lore")?.map {
        ComponentReader<T>(it)
    }

    fun write(t: T) = ItemStack(type).apply {
        itemMeta = itemMeta?.apply {
            displayName(display?.createComponent(t))
            lore(writerLore?.map {
                it.createComponent(t)
            })
            setCustomModelData(writerData)
            addItemFlags(*ItemFlag.values())
            isUnbreakable = true
        }
    }
}