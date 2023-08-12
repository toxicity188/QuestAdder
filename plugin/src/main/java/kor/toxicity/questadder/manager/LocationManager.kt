package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.extension.info
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.extension.warn
import kor.toxicity.questadder.util.NamedLocation
import org.bukkit.Bukkit
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object LocationManager: QuestAdderManager {

    private val locationMap = HashMap<String,NamedLocation>()

    override fun start(adder: QuestAdder) {
        val locationFolder = File(adder.dataFolder,"locations")
        adder.command.addCommandAPI("location", arrayOf("loc","좌표"), "location-related command.", true, CommandAPI("qa loc")
            .addCommand("create") {
                aliases = arrayOf("c","생성")
                description = "add sender's location to yaml."
                usage = "create <file> <key>"
                length = 2
                allowedSender = arrayOf(SenderType.PLAYER)
                executor = { sender, args ->
                    if (locationMap.containsKey(args[2])) {
                        sender.warn("the location named \"${args[2]} already exists.")
                    } else {
                        val location = (sender as Player).location
                        QuestAdder.asyncTask {
                            try {
                                val file = File(locationFolder.apply {
                                    mkdir()
                                }, args[1]).apply {
                                    if (!exists()) createNewFile()
                                }
                                YamlConfiguration().run {
                                    load(file)
                                    set(args[2], MemoryConfiguration().apply {
                                        set("name", args[2])
                                        set("world", location.world.name)
                                        set("x", location.x)
                                        set("y", location.y)
                                        set("z", location.z)
                                        set("pitch", location.pitch)
                                        set("yaw", location.yaw)
                                    })
                                    save(file)
                                }
                                locationMap[args[2]] = NamedLocation(args[2], location)
                                sender.info("the location named \"${args[2]}\" successfully saved.")
                            } catch (ex: Exception) {
                                sender.info("unable to save the location named \"${args[2]}")
                                sender.info("reason: ${ex.message ?: ex.javaClass.simpleName}")
                            }
                        }
                    }
                }
                tabComplete = { _, args ->
                    if (args.size == 2) locationFolder.listFiles()?.filter {
                        it.extension == "yml" && it.name.startsWith(args[1])
                    }?.map {
                        it.nameWithoutExtension
                    } else null
                }
            }
            .addCommand("teleport") {
                aliases = arrayOf("tp","텔레포트")
                description = "teleport specific location."
                usage = "teleport <location> [player]"
                length = 1
                allowedSender = arrayOf(SenderType.PLAYER)
                executor = { sender, args ->
                    locationMap[args[1]]?.let {
                        if (args.size > 2) {
                            Bukkit.getPlayer(args[2])?.let { player ->
                                player.teleport(it.location)
                                sender.info("the player named \"${player.name}\" successfully teleported to \"${args[1]}\".")
                            } ?: sender.warn("the player named \"${args[2]}\" doesn't exist.")
                        } else {
                            (sender as Player).teleport(it.location)
                            sender.info("successfully teleported to \"${args[1]}\".")
                        }
                    } ?: sender.warn("the location named \"${args[2]}\" doesn't exist.")
                }
                tabComplete = { _, args ->
                    if (args.size == 2) locationMap.keys.filter {
                        it.startsWith(args[1])
                    } else null
                }
            })
    }

    override fun reload(adder: QuestAdder) {
        locationMap.clear()
        adder.loadFolder("locations") { file, section ->
            section.getKeys(false).forEach {
                section.getConfigurationSection(it)?.let { c ->
                    NamedLocation.fromConfig(it,c)?.let { location ->
                        locationMap[it] = location
                    } ?: QuestAdder.warn("unable to read this location. ($it in ${file.name})")
                }  ?: QuestAdder.warn("syntax error: the value is not a configuration section. ($it in ${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${locationMap.size} of locations has successfully loaded.")
    }
    fun getLocation(name: String) = locationMap[name]

    override fun end(adder: QuestAdder) {
    }
}