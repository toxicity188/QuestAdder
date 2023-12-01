package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.util.NamedLocation
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object LocationManager: QuestAdderManager {

    private val locationMap = HashMap<String,NamedLocation>()

    override fun start(adder: QuestAdderBukkit) {
        val locationFolder = File(adder.dataFolder,"locations")
        adder.command.addApiCommand("location", {
            aliases = arrayOf("loc", "좌표")
            permissions = arrayOf("questadder.location")
        }, {
            addCommand("create") {
                aliases = arrayOf("c","생성")
                description = "add sender's location to yaml.".asComponent()
                usage = "create ".asComponent().append("<file> <key>".asComponent(NamedTextColor.AQUA))
                length = 2
                allowedSender = arrayOf(SenderType.PLAYER)
                permissions = arrayOf("questadder.location.create")
                executor = { _, sender, args ->
                    if (locationMap.containsKey(args[1])) {
                        sender.warn("the location named \"${args[1]} already exists.")
                    } else {
                        val location = (sender as Player).location
                        QuestAdderBukkit.asyncTask {
                            try {
                                val file = File(locationFolder.apply {
                                    mkdir()
                                },"${args[0]}.yml").apply {
                                    if (!exists()) createNewFile()
                                }
                                val name = (if (args.size > 3) {
                                    args.toMutableList().apply {
                                        removeAt(0)
                                        removeAt(0)
                                        removeAt(0)
                                    }.joinToString(" ")
                                } else args[1])
                                YamlConfiguration().run {
                                    load(file)
                                    set(args[1], MemoryConfiguration().apply {
                                        set("name", name)
                                        set("world", location.world!!.name)
                                        set("x", location.x)
                                        set("y", location.y)
                                        set("z", location.z)
                                        set("pitch", location.pitch)
                                        set("yaw", location.yaw)
                                    })
                                    save(file)
                                }
                                locationMap[args[1]] = NamedLocation(args[1],Material.BOOK, 0, name.colored(), location)
                                sender.info("the location named \"${args[1]}\" successfully saved.")
                            } catch (ex: Exception) {
                                sender.info("unable to save the location named \"${args[1]}")
                                sender.info("reason: ${ex.message ?: ex.javaClass.simpleName}")
                            }
                        }
                    }
                }
                tabCompleter = { _, _, args ->
                    if (args.size == 1) locationFolder.listFiles()?.filter {
                        it.extension == "yml" && it.name.contains(args[0])
                    }?.map {
                        it.nameWithoutExtension
                    } else null
                }
            }
            addCommand("teleport") {
                aliases = arrayOf("tp","텔레포트")
                description = "teleport specific location.".asComponent()
                usage = "teleport ".asClearComponent().append("<location> ".asComponent(NamedTextColor.AQUA)).append("[player]".asComponent(NamedTextColor.DARK_AQUA))
                length = 1
                permissions = arrayOf("questadder.location.teleport")
                allowedSender = arrayOf(SenderType.PLAYER)
                executor = { _, sender, args ->
                    locationMap[args[0]]?.let {
                        if (args.size > 1) {
                            Bukkit.getPlayer(args[1])?.let { player ->
                                QuestAdderBukkit.scheduler.teleport(player, it.location)
                                sender.info("the player named \"${player.name}\" successfully teleported to \"${args[0]}\".")
                            } ?: sender.warn("the player named \"${args[1]}\" doesn't exist.")
                        } else {
                            QuestAdderBukkit.scheduler.teleport(sender as Player, it.location)
                            sender.info("successfully teleported to \"${args[0]}\".")
                        }
                    } ?: sender.warn("the location named \"${args[0]}\" doesn't exist.")
                }
                tabCompleter = { _, _, args ->
                    if (args.size == 1) locationMap.keys.filter {
                        it.contains(args[0])
                    } else null
                }
            }
        })
    }

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
        checker(0.0, "initializing locations...")
        locationMap.clear()
        adder.loadFolder("locations") { file, section ->
            section.getKeys(false).forEach {
                section.getConfigurationSection(it)?.let { c ->
                    NamedLocation.fromConfig(it,c)?.let { location ->
                        locationMap[it] = location
                    } ?: QuestAdderBukkit.warn("unable to read this location. ($it in ${file.name})")
                }  ?: QuestAdderBukkit.warn("syntax error: the value is not a configuration section. ($it in ${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${locationMap.size} of locations has successfully loaded.")
        checker(0.0, "finalizing locations...")
    }
    fun getLocation(name: String) = locationMap[name]

    override fun end(adder: QuestAdderBukkit) {
    }
}
