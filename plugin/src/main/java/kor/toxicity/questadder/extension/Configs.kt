package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.manager.ItemManager
import kor.toxicity.questadder.manager.ResourcePackManager
import kor.toxicity.questadder.util.ResourcePackData
import kor.toxicity.questadder.util.SoundData
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
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
fun ConfigurationSection.findBoolean(vararg string: String) = string.any {
    getBoolean(it)
}
fun ConfigurationSection.findInt(defaultValue: Int = 0, vararg string: String) = string.firstNotNullOfOrNull {
    val i = getInt(it)
    if (i == 0) null else i
} ?: defaultValue
fun ConfigurationSection.findLong(defaultValue: Long = 0, vararg string: String) = string.firstNotNullOfOrNull {
    val i = getLong(it)
    if (i == 0L) null else i
} ?: defaultValue
fun ConfigurationSection.findDouble(defaultValue: Double = 0.0, vararg string: String) = string.firstNotNullOfOrNull {
    val i = getDouble(it)
    if (i == 0.0) null else i
} ?: defaultValue

fun ConfigurationSection.getAsSoundData(key: String) = if (isString(key)) SoundData.fromString(getString(key)!!) else if (isConfigurationSection(key)) SoundData.fromConfig(getConfigurationSection(key)!!) else null

fun ConfigurationSection.getAsStringList(key: String): List<String>? = if (isList(key)) getStringList(key) else if (isString(key)) listOf(getString(key)!!) else null

fun ConfigurationSection.findItemStack(vararg string: String) = string.firstNotNullOfOrNull {
    getAsItemStack(it)
}
fun ConfigurationSection.getAsItemStack(key: String): ItemStack? = if (isItemStack(key)) getItemStack(key) else if (isConfigurationSection(key)) getConfigurationSection(key)!!.run {
    (try {
        Material.valueOf(findString("Type","type")?.uppercase() ?: "APPLE")
    } catch (ex: Exception) {
        Material.APPLE
    }).let { material ->
        ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                setCustomModelData(findInt(0,"CustomModelData","custom-model-data","Data","data"))
                findString("Display","display")?.run {
                    displayName(colored())
                }
                findStringList("Lore","lore")?.run {
                    lore(colored())
                }
                isUnbreakable = findBoolean("Unbreakable","unbreakable")
                ItemFlag.values().forEach {
                    addItemFlags(it)
                }
                findStringList("Attributes","attributes","Attribute","attribute")?.forEach {
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
                findConfig("Icon","icon")?.let {
                    it.findString("Asset","asset")?.let { asset ->
                        ResourcePackManager.addData(ResourcePackData(material,asset) {
                            itemMeta = itemMeta?.apply {
                                setCustomModelData(it)
                            }
                        })
                    }
                }
                findConfig("Enchant","Enchantment","enchant","enchantment")?.let {
                    it.getKeys(false).forEach { key ->
                        val str = it.getString(key) ?: return@forEach
                        val enchant = NamespacedKey.fromString(key)?.let { namespacedKey ->
                            Enchantment.getByKey(namespacedKey)
                        } ?: return@forEach
                        try {
                            addUnsafeEnchantment(enchant,str.toInt())
                        } catch (ex: Exception) {
                            QuestAdder.warn("cannot apply this enchant: $key $str")
                        }
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