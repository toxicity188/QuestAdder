package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.manager.ItemManager
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.regex.Pattern

private const val ATTRIBUTE_NAME = "questadder.attribute"
private val ATTRIBUTE_UUID = UUID.fromString("8d1fc1b6-00fe-11ee-be56-0242ac120002")

private val ITEM_PATTERN = Pattern.compile("\\?(?<type>([a-zA-Z]|_)+) (?<data>[0-9]+) (?<display>(\\w|\\W)+)")


fun ConfigurationSection.findString(vararg string: String) = string.firstNotNullOfOrNull {
    getString(it)
}
fun ConfigurationSection.findStringList(vararg string: String) = string.firstNotNullOfOrNull {
    getAsStringList(it)
}
fun ConfigurationSection.findConfig(vararg string: String) = string.firstNotNullOfOrNull {
    getConfigurationSection(it)
}
fun ConfigurationSection.findInt(defaultValue: Int = 0, vararg string: String) = string.firstNotNullOfOrNull {
    val i = getInt(it)
    if (i == 0) null else i
} ?: defaultValue
fun ConfigurationSection.findDouble(defaultValue: Double = 0.0, vararg string: String) = string.firstNotNullOfOrNull {
    val i = getDouble(it)
    if (i == 0.0) null else i
} ?: defaultValue


fun ConfigurationSection.getAsStringList(key: String): List<String>? = if (isList(key)) getStringList(key) else if (isString(key)) listOf(getString(key)!!) else null

fun ConfigurationSection.getAsItemStack(key: String): ItemStack? = if (isItemStack(key)) getItemStack(key) else if (isConfigurationSection(key)) getConfigurationSection(key)!!.run {
    ItemStack(try {
        Material.valueOf(getString("type")?.uppercase() ?: "APPLE")
    } catch (ex: Exception) {
        Material.APPLE
    }).apply {
        itemMeta = itemMeta?.apply {
            setCustomModelData(getInt("data"))
            getString("display")?.run {
                displayName(colored())
            }
            getAsStringList("lore")?.run {
                lore(colored())
            }
            isUnbreakable = getBoolean("unbreakable",true)
            ItemFlag.values().forEach {
                addItemFlags(it)
            }
            getAsStringList("attributes")?.forEach {
                val split = it.split(' ')
                if (split.size == 4) {
                    try {
                        addAttributeModifier(
                            Attribute.valueOf(split[0].uppercase().replace('.','_')), AttributeModifier(
                                ATTRIBUTE_UUID,
                                ATTRIBUTE_NAME,
                                split[1].toDouble(),
                                AttributeModifier.Operation.valueOf(split[2].uppercase()),
                                EquipmentSlot.valueOf(split[3].uppercase())
                            )
                        )
                    } catch (ex: Exception) {
                        QuestAdder.warn("다음 Attribute를 읽을 수 없습니다: $it")
                    }
                }
            }
        }
    }
} else if (isString(key)) getString(key)!!.let {
    val matcher = ITEM_PATTERN.matcher(it)
    if (matcher.find()) try {
        ItemStack(Material.valueOf(matcher.group("type"))).apply {
            itemMeta = itemMeta?.apply {
                setCustomModelData(matcher.group("data").toInt())
                displayName(matcher.group("display").colored())
            }
        }
    } catch (ex: Exception) {
        null
    } else ItemManager.getItem(it)
} else null