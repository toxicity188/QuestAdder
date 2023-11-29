package kor.toxicity.questadder.manager

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.findString
import kor.toxicity.questadder.extension.send
import org.bukkit.Bukkit
import java.util.UUID

object SkinManager: QuestAdderManager {

    private val profileMap = HashMap<String,GameProfile>()

    override fun start(adder: QuestAdderBukkit) {

    }

    override fun reload(adder: QuestAdderBukkit, checker: (Double, String) -> Unit) {
        checker(0.0, "initializing player skin...")
        profileMap.clear()
        adder.loadFolder("skins") { file, config ->
            config.getKeys(false).forEach {
                config.getConfigurationSection(it)?.let { config ->
                    val value = config.findString("value","Value") ?: run {
                        QuestAdderBukkit.warn("value not found: $it in ${file.name}")
                        return@forEach
                    }
                    val signature = config.findString("Signature","signature") ?: run {
                        QuestAdderBukkit.warn("signature not found: $it in ${file.name}")
                        return@forEach
                    }
                    profileMap[it] = GameProfile(UUID.randomUUID(),"").apply {
                        properties.put("textures", Property("textures", value, signature))
                    }
                } ?: QuestAdderBukkit.warn("unable to read this section: $it in ${file.name}")
            }
        }
        Bukkit.getConsoleSender().send("${profileMap.size} of skins has successfully loaded.")
        checker(0.0, "finializing player skin...")
    }

    fun getProfile(name: String) = profileMap[name]

    override fun end(adder: QuestAdderBukkit) {
    }
}
