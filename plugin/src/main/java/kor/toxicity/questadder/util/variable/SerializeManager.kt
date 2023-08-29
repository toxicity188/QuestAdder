package kor.toxicity.questadder.util.variable

import kor.toxicity.questadder.util.HashedClass
import kor.toxicity.questadder.util.Null
import kor.toxicity.questadder.util.reflect.PrimitiveType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

object SerializeManager {
    private val map = HashMap<HashedClass,VariableSerializer>().apply {
        put(HashedClass(OfflinePlayer::class.java),object : VariableSerializer {
            override fun deserialize(string: String): Any? {
                val player = Bukkit.getOfflinePlayer(UUID.fromString(string))
                return if (player.hasPlayedBefore()) player else null
            }

            override fun serialize(any: Any): String {
                return (any as OfflinePlayer).uniqueId.toString()
            }
        })
        put(HashedClass(Number::class.java), object : VariableSerializer {
            override fun deserialize(string: String): Any {
                return string.toDouble()
            }

            override fun serialize(any: Any): String {
                return any.toString()
            }
        })
        put(HashedClass(String::class.java), object : VariableSerializer {
            override fun serialize(any: Any): String {
                return any.toString()
            }

            override fun deserialize(string: String): Any {
                return string
            }
        })
        val boolean = object : VariableSerializer {
            override fun deserialize(string: String): Any {
                return when (string) {
                    "true" -> true
                    else -> false
                }
            }

            override fun serialize(any: Any): String {
                return any.toString()
            }
        }
        put(HashedClass(PrimitiveType.BOOLEAN.primitive), boolean)
        put(HashedClass(PrimitiveType.BOOLEAN.reference), boolean)
        put(HashedClass(ItemStack::class.java), object : VariableSerializer {
            override fun serialize(any: Any): String {
                return Base64.getEncoder().encodeToString((any as ItemStack).serializeAsBytes())
            }

            override fun deserialize(string: String): Any {
                return ItemStack.deserializeBytes(Base64.getDecoder().decode(string))
            }
        })
        put(HashedClass(Null::class.java), object : VariableSerializer {
            override fun deserialize(string: String): Any? {
                return null
            }

            override fun serialize(any: Any): String {
                return "null"
            }
        })
    }
    fun canSerialize(clazz: Class<*>): Boolean {
        return map.keys.any {
            it.clazz.isAssignableFrom(clazz)
        }
    }

    fun trySerialize(any: Any) = map.firstNotNullOfOrNull {
        if (it.key.clazz.isAssignableFrom(any.javaClass)) try {
            it.value.serialize(any)
        } catch (ex: Exception) {
            null
        } else null
    }

    fun tryDeserialize(clazz: Class<*>, string: String) = map.firstNotNullOfOrNull {
        if (it.key.clazz.isAssignableFrom(clazz)) try {
            it.value.deserialize(string)
        } catch (ex: Exception) {
            null
        } else null
    }
}