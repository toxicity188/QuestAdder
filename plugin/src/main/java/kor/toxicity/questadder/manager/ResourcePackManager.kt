package kor.toxicity.questadder.manager

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import kor.toxicity.questadder.QuestAdder
import org.zeroturnaround.zip.ZipUtil
import java.io.*
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.max

object ResourcePackManager: QuestAdderManager {
    private val gson = Gson()
    private val jsonFaces = arrayOf(
        "north",
        "east",
        "south",
        "west",
        "up",
        "down"
    )
    private val assetsMap = mapOf(
        "arrow" to 1,
        "pillar" to 2
    )

    override fun start(adder: QuestAdder) {
    }

    override fun reload(adder: QuestAdder) {
        try {
            val resource = File(adder.dataFolder, "resources").apply {
                mkdir()
            }
            val build = File(resource, "build").apply {
                deleteRecursively()
                mkdir()
            }
            val icons = File(resource, "icons").apply {
                mkdir()
            }
            val assets = File(build, "assets").apply {
                mkdir()
            }
            val minecraft = File(assets, "minecraft").apply {
                mkdir()
            }
            val questAdder = File(assets, "questadder").apply {
                mkdir()
            }
            val questTextures = File(File(questAdder, "textures").apply {
                mkdir()
            },"item").apply {
                mkdir()
            }
            val questModels = File(questAdder, "models").apply {
                mkdir()
            }
            val minecraftModels = File(File(minecraft, "models").apply {
                mkdir()
            }, "item").apply {
                mkdir()
            }

            assetsMap.keys.forEach { s ->
                File(icons, "$s.bbmodel").let {
                    if (!it.exists()) adder.getResource("$s.bbmodel")?.buffered()?.use { stream ->
                        it.outputStream().buffered().use { bos ->
                            stream.copyTo(bos)
                        }
                    }
                }
            }
            adder.getResource("pack.zip")?.use {
                ZipUtil.unpack(it, build)
            }
            icons.listFiles()?.forEach {
                when (it.extension) {
                    "png" -> readImageModel(it, questModels, questTextures)
                    "bbmodel" -> readBlockBenchModel(it, questModels, questTextures)
                }
            }
            val type = QuestAdder.Config.defaultResourcePackItem.toString().lowercase()
            JsonWriter(File(minecraftModels,"${type}.json").writer().buffered()).use {
                gson.toJson(JsonObject().apply {
                    addProperty("parent","minecraft:item/generated")
                    add("textures",JsonObject().apply {
                        addProperty("layer0","questadder:item/${type}")
                    })
                    add("overrides",JsonArray().apply {
                        assetsMap.forEach {
                            add(JsonObject().apply {
                                add("predicate",JsonObject().apply {
                                    addProperty("custom_model_data",it.value)
                                })
                                addProperty("model","questadder:${it.key}")
                            })
                        }
                    })
                },it)
            }
        } catch (ex: Exception) {
            QuestAdder.warn("unable to make a resource pack.")
        }
    }

    private fun readImageModel(file: File, models: File, textures: File) {
        file.copyTo(File(textures,file.name))
        JsonWriter(File(models,file.name).writer().buffered()).use {
            gson.toJson(JsonObject().apply {
                addProperty("parent","minecraft:item/generated")
                add("textures",JsonObject().apply {
                    addProperty("layer0","questadder:item/${file.nameWithoutExtension}")
                })
            }, it)
        }
    }
    private fun readBlockBenchModel(file: File, models: File, textures: File) {
        val n = file.nameWithoutExtension
        file.reader().buffered().use { fileReader ->
            val element = JsonParser.parseReader(fileReader).asJsonObject
            FileWriter(
                File(
                    models,
                    "$n.json"
                )
            ).use { writer ->
                BufferedWriter(writer).use { bufferedWriter ->
                    JsonWriter(bufferedWriter).use { jsonWriter ->
                        val textureObject = JsonObject()
                        val textureSource = element.getAsJsonArray("textures")

                        for ((index, jsonElement) in textureSource.withIndex()) {
                            val source =
                                jsonElement.asJsonObject.getAsJsonPrimitive("source").asString.split(
                                    ','
                                )[1]
                            val bytes = Base64.getDecoder().decode(source)
                            ByteArrayInputStream(bytes).use { by ->
                                val imageResult = ImageIO.read(by)
                                ImageIO.write(
                                    imageResult, "png",
                                    File(
                                        textures,
                                        "${n}_$index.png"
                                    )
                                )
                            }
                            textureObject.addProperty(
                                index.toString(),
                                "questadder:item/${n}_$index"
                            )
                        }
                        val resolution = element.getAsJsonObject("resolution")
                        val t = max(
                            resolution.getAsJsonPrimitive("width").asInt,
                            resolution.getAsJsonPrimitive("height").asInt
                        ) / 16

                        gson.toJson(JsonObject().apply {
                            add("textures", textureObject)
                            add("elements", JsonArray().apply {
                                for (item in element.getAsJsonArray("elements")) {
                                    val get = item.asJsonObject
                                    add(JsonObject().apply {
                                        add("from", get.getAsJsonArray("from"))
                                        add("to", get.getAsJsonArray("to"))
                                        add(
                                            "rotation",
                                            get.getAsJsonArray("rotation")?.run {
                                                JsonObject().apply {
                                                    val one = get(0).asFloat
                                                    val two = get(1).asFloat
                                                    val three = get(2).asFloat
                                                    if (one != 0F) {
                                                        addProperty("angle", one)
                                                        addProperty("axis", "x")
                                                    } else if (two != 0F) {
                                                        addProperty("angle", two)
                                                        addProperty("axis", "y")
                                                    } else {
                                                        addProperty("angle", three)
                                                        addProperty("axis", "z")
                                                    }
                                                    add(
                                                        "origin",
                                                        get.getAsJsonArray("origin")
                                                    )
                                                }
                                            })
                                        add("faces", JsonObject().apply {
                                            val original = get.getAsJsonObject("faces")
                                            for (jsonFace in jsonFaces) {
                                                original.getAsJsonObject(jsonFace)?.let { arrayGet ->
                                                    add(
                                                        jsonFace,
                                                        JsonObject().apply {
                                                            val textureGet = try {
                                                                arrayGet.getAsJsonPrimitive(
                                                                    "texture"
                                                                ).asString
                                                            } catch (ex: Exception) {
                                                                "0"
                                                            }
                                                            add(
                                                                "uv",
                                                                arrayGet.getAsJsonArray(
                                                                    "uv"
                                                                )
                                                                    .let { originalArray ->
                                                                        JsonArray().apply {

                                                                            fun getFloat(index: Int): Float {
                                                                                return originalArray[index].asFloat
                                                                            }

                                                                            add(getFloat(0) / t)
                                                                            add(getFloat(1) / t)
                                                                            add(getFloat(2) / t)
                                                                            add(getFloat(3) / t)
                                                                        }
                                                                    })
                                                            addProperty(
                                                                "texture",
                                                                "#$textureGet"
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        })
                                    })
                                }
                            })
                            add("display", element.getAsJsonObject("display"))
                        }, jsonWriter)
                    }
                }
            }
        }
    }

    override fun end(adder: QuestAdder) {
    }
}