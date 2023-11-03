package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.util.SoundData
import kor.toxicity.questadder.manager.ItemManager
import kor.toxicity.questadder.manager.ResourcePackManager
import kor.toxicity.questadder.util.ResourcePackData
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

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

fun ConfigurationSection.getAsSoundData(key: String) = if (isString(key)) SoundData.fromString(getString(key)!!) else if (isConfigurationSection(key)) run {
    val sec = getConfigurationSection(key)!!
    sec.findString("Name","name")?.let {
        SoundData(it, sec.findDouble(1.0,"Volume","volume").toFloat(), sec.findDouble(1.0,"Pitch","pitch").toFloat())
    }
} else null

fun ConfigurationSection.findSoundData(vararg string: String) = string.firstNotNullOfOrNull {
    getAsSoundData(it)
}

fun ConfigurationSection.getAsStringList(key: String): List<String>? = if (isList(key)) getStringList(key) else if (isString(key)) listOf(getString(key)!!) else null

fun ConfigurationSection.findItemStack(vararg string: String, apply: (ItemMeta) -> Unit = {}) = string.firstNotNullOfOrNull {
    getAsItemStack(it, apply)
}
fun ConfigurationSection.getAsItemStack(key: String, apply: (ItemMeta) -> Unit = {}): ItemStack? = if (isItemStack(key)) getItemStack(key)!!.apply {
    itemMeta = itemMeta?.apply(apply)
} else if (isConfigurationSection(key)) getConfigurationSection(key)!!.run {
    (try {
        Material.valueOf(findString("Type","type")?.uppercase() ?: "APPLE")
    } catch (ex: Exception) {
        Material.APPLE
    }).let { material ->
        ItemStack(material).apply {
            itemMeta = itemMeta?.also { meta ->
                meta.setCustomModelData(findInt(0,"CustomModelData","custom-model-data","Data","data"))
                findString("Display","display")?.run {
                    QuestAdderBukkit.platform.setDisplay(meta ,colored())
                }
                findStringList("Lore","lore")?.run {
                    QuestAdderBukkit.platform.setLore(meta, colored())
                }
                meta.isUnbreakable = findBoolean("Unbreakable","unbreakable")
                ItemFlag.entries.forEach {
                    meta.addItemFlags(it)
                }
                findStringList("Attributes","attributes","Attribute","attribute")?.forEach {
                    val split = it.split(' ')
                    if (split.size == 4) {
                        try {
                            meta.addAttributeModifier(
                                Attribute.valueOf(split[0].uppercase().replace('.','_')), AttributeModifier(
                                    ATTRIBUTE_UUID,
                                    ATTRIBUTE_NAME,
                                    split[1].toDouble(),
                                    AttributeModifier.Operation.valueOf(split[2].uppercase()),
                                    EquipmentSlot.valueOf(split[3].uppercase())
                                )
                            )
                        } catch (ex: Exception) {
                            QuestAdderBukkit.warn("unable to read that attribute: $it")
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
                            QuestAdderBukkit.warn("cannot apply this enchant: $key $str")
                        }
                    }
                }
                apply(meta)
            }
        }
    }
} else if (isString(key)) getString(key)!!.let {
    val matcher = ITEM_PATTERN.matcher(it)
    if (matcher.find()) try {
        ItemStack(Material.valueOf(matcher.group("type"))).apply {
            itemMeta = itemMeta?.apply {
                setCustomModelData(matcher.group("data").toInt())
                QuestAdderBukkit.platform.setDisplay(this, matcher.group("display").colored())
            }
        }
    } catch (ex: Exception) {
        null
    } else ItemManager.getItem(it)
} else null

fun ConfigurationSection.copy(): ConfigurationSection {
    fun copy0(any: Any): Any {
        return if (any is ConfigurationSection) {
            val config = MemoryConfiguration()
            any.getKeys(false).forEach {
                any.get(it)?.let { any0 ->
                    config.set(it,copy0(any0))
                }
            }
            config
        } else any
    }
    val config = MemoryConfiguration()
    getKeys(false).forEach {
        get(it)?.let { any ->
            config.set(it, copy0(any))
        }
    }
    return config
}

inline fun <reified T> Array<T>.addElement(element: T) = toMutableList().apply {
    add(element)
}.toTypedArray()
inline fun <reified T> Array<T>.addElement(index: Int, element: T): Array<T> {
    return if (index > lastIndex) addElement(element) else toMutableList().apply {
        add(index, element)
    }.toTypedArray()
}
inline fun <reified T> Array<T>.removeLast(): Array<T>? {
    if (size <= 1) return null
    return toMutableList().apply {
        removeAt(lastIndex)
    }.toTypedArray()
}
inline fun <reified T> Array<T>.removeAt(int: Int): Array<T> = toMutableList().apply {
    removeAt(int)
}.toTypedArray()

private val INT_PATTERN = Pattern.compile("([0-9]+)")
fun ConfigurationSection.rebase(pos: Int, add: Int) {
    val tree = TreeMap<Int, Any>()
    getKeys(false).forEach {
        if (!INT_PATTERN.matcher(it).find()) return@forEach
        val int = it.toInt()
        val any = get(it) ?: return@forEach
        if (int > pos) tree[int + add] = any
        else tree[int] = any
        set(it, null)
    }
    tree.forEach {
        set(it.key.toString(), it.value)
    }
}
