package kor.toxicity.questadder.data

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kor.toxicity.questadder.api.mechanic.IQuestData
import kor.toxicity.questadder.api.mechanic.QuestRecord
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.util.Base64

class QuestData (
    val time: LocalDateTime,
    var state: QuestRecord,
    val variable: MutableMap<String,Long>
): IQuestData {
    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapterFactory(object : TypeAdapterFactory {
                override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
                    val delegate = gson.getDelegateAdapter(this,type)
                    return object : TypeAdapter<T>() {
                        override fun write(writer: JsonWriter, value: T) {
                            delegate.write(writer,value)
                        }
                        override fun read(p0: JsonReader): T? {
                            return try {
                                delegate.read(p0)
                            } catch (ex: Exception) {
                                p0.skipValue()
                                null
                            }
                        }
                    }
                }
            })
            .registerTypeAdapter(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime> {
                override fun deserialize(p0: JsonElement, p1: Type, p2: JsonDeserializationContext): LocalDateTime {
                    return LocalDateTime.parse(p0.asString)
                }

            })
            .registerTypeAdapter(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime> {
                override fun serialize(p0: LocalDateTime, p1: Type, p2: JsonSerializationContext): JsonElement {
                    return JsonPrimitive(p0.toString())
                }

            })
            .create()
        fun deserialize(json: String) = try {
            gson.fromJson(String(Base64.getDecoder().decode(json)),QuestData::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    fun serialize(): String = Base64.getEncoder().encodeToString(gson.toJson(this).toByteArray())
    override fun getGivenTime(): LocalDateTime {
        return time
    }

    override fun getLocalVariableMap(): MutableMap<String, Long> {
        return variable
    }

    override fun getRecord(): QuestRecord {
        return state
    }
}
