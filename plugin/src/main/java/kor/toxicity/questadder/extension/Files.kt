package kor.toxicity.questadder.extension

import kor.toxicity.questadder.QuestAdderBukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

fun File.loadYamlFolder(action: (File, ConfigurationSection) -> Unit) {
    if (!exists()) mkdir()
    listFiles()?.forEach {
        if (it.extension == "yml") try {
            YamlConfiguration().run {
                load(it)
                action(it,this)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            QuestAdderBukkit.warn("unable to read this file: ${it.name}")
        }
    }
}