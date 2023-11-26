package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdderBukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.regex.Pattern

object ConstantManager: QuestAdderManager {

    private val constantMap = HashMap<String, Any>()
    private val numberPattern = Pattern.compile("^(([0-9]+)(\\.?([0-9]+)))$")

    override fun start(adder: QuestAdderBukkit) {

    }

    override fun reload(adder: QuestAdderBukkit) {
        try {
            val yaml = YamlConfiguration.loadConfiguration(File(adder.dataFolder.apply {
                if (!exists()) mkdir()
            }, "constants.yml").apply {
                if (!exists()) createNewFile()
            })
            yaml.getKeys(false).forEach {
                yaml.getString(it)?.let { s ->
                    when (s) {
                        "true", "false" -> constantMap[it] = s.toBoolean()
                        else -> {
                            val matcher = numberPattern.matcher(s)
                            if (matcher.find()) {
                                try {
                                    constantMap[it] = s.toDouble()
                                } catch (ex: Exception) {
                                    QuestAdderBukkit.warn("unable to parse that string: $s")
                                }
                            } else {
                                constantMap[it] = s
                            }
                        }
                    }
                } ?: QuestAdderBukkit.warn("unable to read this constant: $it")
            }
        } catch (ex: Exception) {
            QuestAdderBukkit.warn("unable to load constants.yml")
            QuestAdderBukkit.warn("reason: ${ex.message}")
        }
    }
    fun getConstant(name: String) = constantMap[name]

    override fun end(adder: QuestAdderBukkit) {
    }
}
