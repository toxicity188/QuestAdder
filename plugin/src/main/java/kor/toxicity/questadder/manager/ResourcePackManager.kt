package kor.toxicity.questadder.manager

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.extension.parseChar
import kor.toxicity.questadder.util.ResourcePackData
import org.bukkit.Material
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.zeroturnaround.zip.ZipUtil
import java.io.*
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
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
        1 to "arrow",
        2 to "pillar",
        3 to "request",
        4 to "complete"
    )

    private val emptyConfig = MemoryConfiguration()
    private val fontMap = HashMap<String,String>()

    override fun start(adder: QuestAdder) {
    }

    private var dataList = ArrayList<ResourcePackData>()
    fun addData(data: ResourcePackData) {
        dataList.add(data)
    }
    fun getImageFont(name: String) = fontMap[name]

    override fun reload(adder: QuestAdder) {
        val resource = File(adder.dataFolder, "resources").apply {
            mkdir()
        }
        val build = File(resource, "build").apply {
            deleteRecursively()
            mkdir()
        }
        val fonts = File(resource,"fonts").apply {
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
        val questTextures = File(questAdder, "textures").apply {
            mkdir()
        }
        val questItemTextures = File(questTextures,"item").apply {
            mkdir()
        }
        val questFontTextures = File(questTextures,"font").apply {
            mkdir()
        }
        val questModels = File(questAdder, "models").apply {
            mkdir()
        }
        val questFont = File(questAdder, "font").apply {
            mkdir()
        }
        val minecraftModels = File(File(minecraft, "models").apply {
            mkdir()
        }, "item").apply {
            mkdir()
        }

        val fontPngMap = LinkedHashMap<String,File>()
        val fontYmlMap = LinkedHashMap<String,File>()
        fonts.listFiles()?.forEach {
            when (it.extension) {
                "png" -> fontPngMap[it.nameWithoutExtension] = it
                "yml" -> fontYmlMap[it.nameWithoutExtension] = it
            }
        }
        val json = JsonArray()
        var i = 0xD0000
        fontMap.clear()
        fontPngMap.forEach {
            val config = fontYmlMap[it.key]?.let { yaml ->
                try {
                    YamlConfiguration().apply {
                        load(yaml)
                    }
                } catch (ex: Exception) {
                    QuestAdder.warn("unable to read this file: ${yaml.name}")
                    null
                }
            } ?: emptyConfig
            val char = (i++).parseChar()
            fontMap[it.key] = char
            it.value.copyTo(File(questFontTextures,it.value.name))
            json.add(JsonObject().apply {
                addProperty("type","bitmap")
                addProperty("file","questadder:font/${it.value.name}")
                addProperty("ascent",config.getInt("ascent",0))
                addProperty("height",config.getInt("height",8))
                add("chars",JsonArray().apply {
                    add(char)
                })
            })
        }
        JsonWriter(File(questFont,"build.json").writer().buffered()).use {
            gson.toJson(JsonObject().apply {
                add("providers",json)
            },it)
        }

        adder.addLazyTask {
            try {
                assetsMap.values.forEach { s ->
                    File(icons, "$s.bbmodel").let {
                        if (!it.exists()) adder.getResource("$s.bbmodel")?.buffered()?.use { stream ->
                            it.outputStream().buffered().use { bos ->
                                stream.copyTo(bos)
                            }
                        }
                        if (it.exists()) readBlockBenchModel(it, questModels, questItemTextures)
                    }
                }
                adder.getResource("pack.zip")?.use {
                    ZipUtil.unpack(it, build)
                }

                val fileMap = HashMap<String,File>()
                fun addFile(file: File, prefix: String) {
                    file.listFiles()?.forEach {
                        if (it.isDirectory) addFile(it,"$prefix${it.name}/")
                        else fileMap["$prefix${it.nameWithoutExtension}"] = it
                    }
                }
                addFile(icons,"")

                val materialMap = EnumMap<Material,MutableMap<Int,String>>(Material::class.java)
                val loadAssetsMap = HashMap<String,Int>()

                dataList.forEach {
                    fileMap[it.assets]?.let { file ->
                        loadAssetsMap[it.assets]?.let {int ->
                            it.action(int)
                            return@forEach
                        }
                        val map = materialMap.getOrPut(it.material) {
                            TreeMap()
                        }
                        var modelData = it.assets.hashCode()
                        while (map.containsKey(modelData)) modelData += 1
                        map[modelData] = it.assets
                        loadAssetsMap[it.assets] = modelData
                        it.action(modelData)
                        when (file.extension) {
                            "png" -> readImageModel(file,questModels,questItemTextures)
                            "bbmodel" -> readBlockBenchModel(file,questModels,questItemTextures)
                        }
                    }
                }
                dataList.clear()

                fun saveCustomModelData(material: Material, entry: Collection<Map.Entry<Int,String>>) {
                    val type = material.toString().lowercase()
                    JsonWriter(File(minecraftModels,"${type}.json").writer().buffered()).use {
                        gson.toJson(JsonObject().apply {
                            addProperty("parent","minecraft:item/generated")
                            add("textures",JsonObject().apply {
                                addProperty("layer0","minecraft:item/${type}")
                            })
                            add("overrides",JsonArray().apply {
                                entry.forEach {
                                    add(JsonObject().apply {
                                        add("predicate",JsonObject().apply {
                                            addProperty("custom_model_data",it.key)
                                        })
                                        addProperty("model","questadder:${it.value}")
                                    })
                                }
                            })
                        },it)
                    }
                }
                saveCustomModelData(QuestAdder.Config.defaultResourcePackItem, assetsMap.entries)
                materialMap.forEach {
                    saveCustomModelData(it.key,it.value.entries)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                QuestAdder.warn("unable to make a resource pack.")
            }
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