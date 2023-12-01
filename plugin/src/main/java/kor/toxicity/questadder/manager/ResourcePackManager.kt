package kor.toxicity.questadder.manager

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.event.QuestBlockBreakEvent
import kor.toxicity.questadder.api.event.QuestBlockInteractEvent
import kor.toxicity.questadder.api.event.QuestBlockPlaceEvent
import kor.toxicity.questadder.block.*
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.font.GuiFontData
import kor.toxicity.questadder.font.QuestFont
import kor.toxicity.questadder.font.ToolTip
import kor.toxicity.questadder.font.ToolTipFontData
import kor.toxicity.questadder.manager.registry.BlockRegistry
import kor.toxicity.questadder.util.ResourcePackData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import org.zeroturnaround.zip.ZipUtil
import ru.beykerykt.minecraft.lightapi.common.LightAPI
import java.io.*
import java.util.*
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlin.math.max

object ResourcePackManager: QuestAdderManager {
    private val gson = GsonBuilder().disableHtmlEscaping().create()
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

    private val tooltipMap = HashMap<String,ToolTip>()

    val blockRegistry = BlockRegistry()
    override fun start(adder: QuestAdderBukkit) {
        adder.command.addApiCommand("block", {
            aliases = arrayOf("bl", "블럭")
            permissions = arrayOf("questadder.block")
        }, {
            addCommand("get") {
                aliases = arrayOf("g")
                description = "get the editor of block.".asComponent()
                usage = "get ".asClearComponent().append("<name>".asComponent(NamedTextColor.AQUA))
                length = 1
                allowedSender = arrayOf(SenderType.PLAYER)
                executor = { _, c, a ->
                    (c as Player).give(ItemStack(Material.NOTE_BLOCK).apply {
                        itemMeta = itemMeta?.apply {
                            QuestAdderBukkit.platform.setDisplay(this, "Block editor - ${a[0]}".asClearComponent().color(NamedTextColor.YELLOW))
                            QuestAdderBukkit.platform.setLore(this, listOf(
                                "Right click - place".asClearComponent().color(NamedTextColor.GRAY)
                            ))
                            persistentDataContainer.set(blockEditorKey, PersistentDataType.STRING, a[0])
                        }
                    })
                    c.info("successfully given.")
                }
                tabCompleter = { _, _, a ->
                    if (a.size == 1) blockRegistry.allKeys.filter {
                        it.contains(a[0])
                    } else null
                }
            }
        })
        Bukkit.getPluginManager().registerEvents(object : Listener {
            private val vector = arrayOf(
                Vector(0,1,0),
                Vector(0,-1,0),
                Vector(1,0,0),
                Vector(-1,0,0),
                Vector(0,0,1),
                Vector(0,0,-1)
            )
            @EventHandler
            fun interact(e: PlayerInteractEvent) {
                e.clickedBlock?.let {
                    update(it.location)
                    blockRegistry.get(it.blockData)?.let { b ->
                        QuestBlockInteractEvent(b,e).call()
                    }
                }
            }
            @EventHandler
            fun note(e: NotePlayEvent) {
                if (blockRegistry.get(e.block.blockData) != null) e.isCancelled = true
            }
            @EventHandler
            fun burn(e: BlockBurnEvent) {
                blockRegistry.get(e.block.blockData)?.let {
                    if (!it.bluePrint.canBurned) e.isCancelled = true
                }
            }
            @EventHandler
            fun blockBreak(e: BlockBreakEvent) {
                update(e.block.location)
                blockRegistry.get(e.block.blockData)?.let {
                    QuestBlockBreakEvent(it,e).call()
                    e.isCancelled = true
                    val b = e.block
                    b.setBlockData(Material.AIR.createBlockData(),false)
                    if (Bukkit.getPluginManager().isPluginEnabled("LightAPI")) {
                        val loc = b.location
                        if (it.bluePrint.light != 0) try {
                            val get = LightAPI.get().getLightLevel(loc.world!!.name, loc.blockX, loc.blockY, loc.blockZ)
                            LightAPI.get().setLightLevel(loc.world!!.name, loc.blockX, loc.blockY, loc.blockZ, get - it.bluePrint.light)
                        } catch (ex: Throwable) {
                            QuestAdderBukkit.warn("An error has occurred while using LightAPI.")
                        }
                    }
                }
            }
            @EventHandler
            fun place(e: BlockPlaceEvent) {
                update(e.block.location)
                e.itemInHand.itemMeta?.persistentDataContainer?.get(blockEditorKey, PersistentDataType.STRING)?.let {
                    e.isCancelled = true
                    val loc = e.block.location
                    QuestAdderBukkit.task(loc) {
                        blockRegistry.get(it)?.place(loc)
                    }
                }
                blockRegistry.get(e.block.blockData)?.let {
                    QuestBlockPlaceEvent(it,e).call()
                }
            }
            @EventHandler
            fun ignite(e: BlockIgniteEvent) {
                e.ignitingBlock?.let {
                    if (blockRegistry.get(it.blockData) != null) e.isCancelled = true
                }
            }


            private fun update(location: Location) {
                val taskArray = ArrayList<() -> Unit>()
                vector.forEach {
                    val newLoc = location.clone().add(it)
                    blockRegistry.get(newLoc.block.blockData)?.let {
                        taskArray.add {
                            it.place(newLoc)
                        }
                    }
                }
                if (taskArray.isNotEmpty()) QuestAdderBukkit.task(location) {
                    taskArray.forEach {
                        it()
                    }
                }
            }
        },adder)
    }

    private var dataList = ArrayList<ResourcePackData>()
    fun addData(data: ResourcePackData) {
        dataList.add(data)
    }
    fun getImageFont(name: String) = fontMap[name]

    private val blockEditorKey = NamespacedKey.fromString("questadder.block.editor")!!
    private val guiMap = HashMap<String, GuiFontData>()

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
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
        val tooltips = File(resource, "tooltips").apply {
            mkdir()
        }
        val blocks = File(resource, "blocks").apply {
            mkdir()
        }
        val icons = File(resource, "icons").apply {
            mkdir()
        }
        val guis = File(resource, "guis").apply {
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
        val questBlockTextures = File(questTextures, "block").apply {
            mkdir()
        }
        val questModels = File(questAdder, "models").apply {
            mkdir()
        }
        val questBlockModels = File(questModels, "block").apply {
            mkdir()
        }
        val questFont = File(questAdder, "font").apply {
            mkdir()
        }
        val questFontTooltip = File(questFont, "tooltip").apply {
            mkdir()
        }
        val questFontTexturesTooltip = File(questFontTextures,"tooltip").apply {
            mkdir()
        }
        val questFontTexturesGui = File(questFontTextures,"gui").apply {
            mkdir()
        }
        val minecraftModels = File(File(minecraft, "models").apply {
            mkdir()
        }, "item").apply {
            mkdir()
        }
        val minecraftBlockStates = File(minecraft,"blockstates").apply {
            mkdir()
        }

        //font
        checker(0.0, "initializing font resource pack...")
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
                    QuestAdderBukkit.warn("unable to read this file: ${yaml.name}")
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

        //Guis
        checker(0.25, "initializing gui resource pack...")
        guiMap.clear()
        val guiArray = JsonArray()
        var guiIndex = 0xD0000
        guis.listFiles()?.forEach {
            if (it.extension == "png") {
                try {
                    val yml = YamlConfiguration().apply {
                        File(guis, "${it.nameWithoutExtension}.yml").run {
                            if (exists()) load(this)
                        }
                    }
                    val png = ImageIO.read(it)
                    it.copyTo(File(questFontTexturesGui, it.name))
                    guiArray.add(JsonObject().apply {
                        val height = yml.findInt(256,"Height","height")
                        addProperty("type", "bitmap")
                        addProperty("file", "questadder:font/gui/${it.name}")
                        addProperty("ascent", yml.findInt(0,"Ascent","ascent").coerceAtMost(height))
                        addProperty("height", height)
                        add("chars", JsonArray().apply {
                            add(guiIndex.parseChar())
                        })
                    })
                    guiMap[it.nameWithoutExtension] = GuiFontData(
                        png.height,
                        png.width,
                        Component.text(guiIndex.parseChar()).font(GUI_FONT)
                    )
                    guiIndex++
                } catch (ex: Exception) {
                    QuestAdderBukkit.warn("unable to read this png: ${it.name}")
                    QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                }
            }
        }
        if (!guiArray.isEmpty) {
            JsonWriter(File(questFont, "gui.json").bufferedWriter()).use {
                gson.toJson(JsonObject().apply {
                    add("providers", guiArray)
                }, it)
            }
        }

        //Tooltips
        checker(0.5, "initializing tooltip resource pack...")
        tooltipMap.clear()
        tooltips.listFiles()?.forEach {
            when (it.extension) {
                "ttf","otf" -> {
                    try {
                        val name = it.nameWithoutExtension
                        val yaml = File(tooltips, "$name.yml")
                        if (!yaml.exists()) return@forEach
                        val config = YamlConfiguration().apply {
                            load(yaml)
                        }
                        val fontSize = config.findInt(32, "size", "Size")

                        val talker = config.findConfig("talker","Talker") ?: return@forEach
                        val talk = config.findConfig("talk","Talk") ?: return@forEach


                        val talkerHeight = talker.findInt(8, "Height", "height", "size", "Size").coerceAtLeast(1)
                        val talkerAscent = talker.findInt(0, "ascent", "Ascent", "y", "Y").coerceAtMost(talkerHeight)

                        val talkHeight = talk.findInt(8, "Height", "height", "size", "Size").coerceAtLeast(1)
                        val talkAscent = talk.findInt(0, "ascent", "Ascent", "y", "Y").coerceAtMost(talkHeight)

                        val gui = guiMap[config.findString("gui","Gui") ?: return@forEach] ?: return@forEach
                        val chatOffset = config.findInt(0, "chat-offset", "ChatOffset", "chat-x", "ChatX")

                        val width = config.findDouble(0.0, "width", "Width")
                        val widthMultiplier = talk.findDouble(1.0, "width-multiplier", "WidthMultiplier")
                        val fade = config.findBoolean("fade")
                        tooltipMap[name] = ToolTip(
                            gui,
                            fade,
                            config.findInt(16, "split", "Split"),
                            config.findInt(0, "Offset", "offset", "x", "X"),
                            chatOffset,
                            ToolTipFontData(talker.findInt(0,"offset","Offset", "x", "X"), QuestFont(
                                it,
                                fontSize,
                                talkerHeight,
                                talkerAscent,
                                width,
                                widthMultiplier,
                                talker.findInt(talkerHeight, "blank", "Blank"),
                                questFontTooltip,
                                questFontTexturesTooltip
                            )),
                            (0..<config.findInt(1,"line","Line").coerceAtLeast(1)).map { int ->
                                ToolTipFontData(talk.findInt(0,"offset","Offset", "x", "X"), QuestFont(
                                    it,
                                    fontSize,
                                    talkHeight,
                                    talkAscent - talkHeight * int - talkerHeight,
                                    width,
                                    widthMultiplier,
                                    talk.findInt(talkHeight, "blank", "Blank"),
                                    questFontTooltip,
                                    questFontTexturesTooltip
                                ))
                            }
                        )
                    } catch (ex: Exception) {
                        QuestAdderBukkit.warn("unable to read this tooltip: ${it.name}")
                        QuestAdderBukkit.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    }
                }
            }
        }

        //block
        checker(0.75, "initializing block resource pack...")
        blockRegistry.clear()
        val noteBlockMap = HashMap<String,String>()
        val stringBlockMap = HashMap<String,String>()
        val fireBlockMap = HashMap<String,String>()
        val stemBlockMap = HashMap<String,String>()
        val brownMushroomMap = HashMap<String,String>()
        val redMushroomMap = HashMap<String,String>()
        val chorusPlantMap = HashMap<String,String>()
        blocks.loadYamlFolder { _, c ->
            c.getKeys(false).forEach {
                c.getConfigurationSection(it)?.let { config ->
                    config.getString("icon")?.let { icon ->
                        val file = File(icons,icon)
                        if (!file.exists()) {
                            QuestAdderBukkit.warn("the icon named \"$icon\" doesn't exist.")
                            return@forEach
                        }
                        blockRegistry.tryRegister(it, config)?.let { data ->
                            when (file.extension) {
                                "png" -> readImageBlockModel(file, questBlockModels, questBlockTextures)
                                "bbmodel" -> readBlockBenchModel(file, questBlockModels, questBlockTextures, "block")
                            }
                            when (data) {
                                is NoteBlockData -> noteBlockMap[data.toKey()] = file.nameWithoutExtension
                                is StringBlockData -> stringBlockMap[data.toKey()] = file.nameWithoutExtension
                                is FireBlockData -> fireBlockMap[data.toKey()] = file.nameWithoutExtension
                                is MushroomStemBlockData -> stemBlockMap[data.toKey()] = file.nameWithoutExtension
                                is BrownMushroomBlockData -> brownMushroomMap[data.toKey()] = file.nameWithoutExtension
                                is RedMushroomBlockData -> redMushroomMap[data.toKey()] = file.nameWithoutExtension
                                is ChorusPlantBlockData -> chorusPlantMap[data.toKey()] = file.nameWithoutExtension
                            }
                        }
                    }
                }
            }
        }
        fun saveHashMap(map: HashMap<String,String>, blockName: String) {
            if (map.isNotEmpty()) {
                JsonWriter(File(minecraftBlockStates,"$blockName.json").writer().buffered()).use {
                    gson.toJson(
                        JsonObject().apply {
                            add("variants",JsonObject().apply {
                                map.forEach { e ->
                                    add(e.key, JsonObject().apply {
                                        addProperty("model", "questadder:block/${e.value}")
                                    })
                                }
                            })
                        },
                        it
                    )
                }
            }
        }
        saveHashMap(noteBlockMap,"note_block")
        saveHashMap(stringBlockMap,"tripwire")
        saveHashMap(fireBlockMap,"fire")
        saveHashMap(stemBlockMap,"mushroom_stem")
        saveHashMap(brownMushroomMap,"brown_mushroom_block")
        saveHashMap(redMushroomMap,"red_mushroom_map")
        saveHashMap(chorusPlantMap,"chorus_plant")


        //item
        adder.addLazyTask {
            try {
                assetsMap.values.forEach { s ->
                    File(icons, "$s.bbmodel").let {
                        if (!it.exists()) adder.getResource("$s.bbmodel")?.buffered()?.use { stream ->
                            it.outputStream().buffered().use { bos ->
                                stream.copyTo(bos)
                            }
                        }
                        if (it.exists()) readBlockBenchModel(it, questModels, questItemTextures, "item")
                    }
                }
                adder.getResource("pack.zip")?.use {
                    ZipUtil.unpack(it, build)
                }

                val materialMap = EnumMap<Material,MutableMap<Int,String>>(Material::class.java)
                val loadAssetsMap = HashMap<String,Int>()

                dataList.forEach {
                    val file = File(icons, it.assets)
                    if (!file.exists()) {
                        QuestAdderBukkit.warn("This file doesn't exist: ${file.name}")
                        return@forEach
                    }
                    loadAssetsMap[it.assets]?.let { int ->
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
                        "bbmodel" -> readBlockBenchModel(file,questModels,questItemTextures, "item")
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
                saveCustomModelData(QuestAdderBukkit.Config.defaultResourcePackItem, assetsMap.entries)
                materialMap.forEach {
                    saveCustomModelData(it.key,it.value.entries)
                }
                val buildPath = build.path
                val parent = adder.dataFolder.parentFile
                QuestAdderBukkit.Config.importOtherResourcePack.forEach {
                    val importFile = File(parent,it)
                    if (importFile.exists() && importFile.isDirectory) {
                        fun copy(from: File, to: File) {
                            if (from.isDirectory) {
                                from.listFiles()?.forEach { file ->
                                    copy(file, File(to,file.name))
                                }
                            } else {
                                if (!to.exists()) from.copyTo(to)
                            }
                        }
                        copy(importFile, build)
                    } else {
                        QuestAdderBukkit.warn("this file doesn't exist or is not directory: ${importFile.name}")
                    }
                }
                if (QuestAdderBukkit.Config.zipResourcePack) ZipOutputStream(FileOutputStream(File(resource,"build.zip").apply {
                    if (!exists()) delete()
                }).buffered()).use {
                    fun zip(file: File) {
                        if (file.isFile) {
                            val byte = file.readBytes()
                            val entry = ZipEntry(file.path.substring(buildPath.length + 1).replace('\\','/'))
                            it.putNextEntry(entry)
                            it.write(byte)
                            it.closeEntry()
                        } else {
                            file.listFiles()?.forEach { f ->
                                zip(f)
                            }
                        }
                    }
                    it.setLevel(Deflater.BEST_COMPRESSION)
                    it.setComment("This is an example zip file.")
                    build.listFiles()?.forEach { f ->
                        zip(f)
                    }
                    adder.getResource("pack.png")?.buffered()?.use { png ->
                        val entry = ZipEntry("pack.png")
                        it.putNextEntry(entry)
                        it.write(png.readBytes())
                        it.closeEntry()
                    }
                    val entry = ZipEntry("pack.mcmeta")
                    it.putNextEntry(entry)
                    it.write(gson.toJson(JsonObject().apply {
                        add("pack",JsonObject().apply {
                            addProperty("pack_format", QuestAdderBukkit.nms.getVersion().mcmetaVersion)
                            addProperty("description","QuestAdder's example resource pack.")
                        })
                    }).toByteArray())
                    it.closeEntry()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                QuestAdderBukkit.warn("unable to make a resource pack.")
            }
            0
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
    private fun readImageBlockModel(file: File, models: File, textures: File) {
        file.copyTo(File(textures,file.name))
        JsonWriter(File(models,file.name).writer().buffered()).use {
            gson.toJson(JsonObject().apply {
                addProperty("parent","minecraft:block/cube_all")
                add("textures",JsonObject().apply {
                    addProperty("all","questadder:block/${file.nameWithoutExtension}")
                })
            }, it)
        }
    }
    private fun readBlockBenchModel(file: File, models: File, textures: File, textureLink: String) {
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
                                "questadder:$textureLink/${n}_$index"
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

    fun getToolTip(name: String) = tooltipMap[name]

    override fun end(adder: QuestAdderBukkit) {
    }
}
