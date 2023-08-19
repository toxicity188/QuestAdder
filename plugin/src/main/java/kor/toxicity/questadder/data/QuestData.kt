package kor.toxicity.questadder.data

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kor.toxicity.questadder.mechanic.quest.QuestState
import java.time.LocalDateTime
import java.util.Base64

class QuestData (
    val time: LocalDateTime,
    var state: QuestState,
    val variable: MutableMap<String,Long>
) {
    companion object {
        private val gson = Gson()
            .newBuilder()
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
            .create()
        fun deserialize(json: String) = try {
            val data = gson.fromJson(String(Base64.getDecoder().decode(json)),QuestGsonData::class.java)
            QuestData(
                LocalDateTime.parse(data.time),
                data.state,
                data.variable
            )
        } catch (ex: Exception) {
            null
        }
    }

    fun serialize(): String = Base64.getEncoder().encodeToString(gson.toJson(QuestGsonData(time.toString(),state,variable)).toByteArray())

    private data class QuestGsonData(
        val time: String,
        val state: QuestState,
        val variable: MutableMap<String,Long>
    )
}