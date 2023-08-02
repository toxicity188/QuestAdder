package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.ActionInvokeEvent
import kor.toxicity.questadder.event.QuestInvokeEvent
import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findInt
import kor.toxicity.questadder.extension.findString
import kor.toxicity.questadder.extension.findStringList
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.ItemWriter
import kor.toxicity.questadder.util.action.AbstractAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.function.WrappedFunction
import net.kyori.adventure.text.Component
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class Quest(adder: QuestAdder, file: File, val key: String, section: ConfigurationSection) {

    private val item = section.findConfig("Item","item","Icon","icon")?.let { c ->
        try {
            ItemWriter<QuestInvokeEvent>(c)
        } catch (ex: Exception) {
            null
        }
    } ?: throw RuntimeException("the quest has no icon.")
    private val condition = ArrayList<WrappedFunction>()

    init {
        section.findStringList("Conditions","Condition","conditions","condition")?.forEach { s ->
            condition.add(FunctionBuilder.evaluate(s))
        }

        section.findConfig("variable","Variable","variables","Variables")?.let { c ->
            c.getKeys(false).forEach {
                c.getConfigurationSection(it)?.let { config ->
                    val name = config.findString("name","Name") ?: run {
                        QuestAdder.warn("syntax error: the variable must be named. ($key in ${file.name})")
                        return@forEach
                    }
                    val lore = config.findString("lore","Lore")?.let { s ->
                        ComponentReader<QuestInvokeEvent>(s)
                    }
                    val event = config.findStringList("Event","event","Events","events") ?: run {
                        QuestAdder.warn("syntax error: the variable must be have some event. ($key in ${file.name})")
                        return@forEach
                    }
                    val conditions = config.findStringList("Conditions","Condition","conditions","condition")?.map {s ->
                        FunctionBuilder.evaluate(s)
                    } ?: emptyList()
                    val max = config.findInt(0,"Max","max").toLong()
                    val obj: AbstractAction = object : AbstractAction(adder) {
                        override fun invoke(player: Player, event: ActionInvokeEvent) {
                            val questEvent = QuestInvokeEvent(this@Quest,player).apply {
                                callEvent()
                            }
                            if (conditions.all { wf ->
                                    val t = wf.apply(questEvent)
                                    t is Boolean && t
                                }) {
                                val data  = QuestAdder.getPlayerData(player) ?: return
                                val newValue = (data.getQuestVariable(key,name) ?: 0)
                                if (newValue + 1 == max) {
                                    lore?.createComponent(questEvent)?.let { component ->
                                        QuestAdder.nms.sendAdvancementMessage(player,component)
                                    }
                                }
                                data.setQuestVariable(key,name,(newValue + 1).coerceAtMost(max))
                            }
                        }
                    }
                    for (s in event) {
                        ActionBuilder.createEvent(adder,obj,s)
                    }

                } ?: QuestAdder.warn("syntax error: the value $it is not configuration section. ($key in ${file.name})")
            }
        }
    }
    fun give(player: Player) {
        QuestAdder.getPlayerData(player)?.giveQuest(key)
    }
    fun remove(player: Player) {
        QuestAdder.getPlayerData(player)?.removeQuest(key)
    }
    fun complete(player: Player) {

    }

    fun success(player: Player): Boolean {
        val invokeEvent = QuestInvokeEvent(this,player).apply {
            callEvent()
        }
        return condition.all {
            val get = it.apply(invokeEvent)
            get is Boolean && get
        }
    }
    fun getIcon(player: Player): ItemStack {
        val event = QuestInvokeEvent(this,player).apply {
            callEvent()
        }
        return item.write(event).apply {
            if (condition.all {
                    val get = it.apply(event)
                    get is Boolean && get
                }) itemMeta = itemMeta?.apply {
                addEnchant(Enchantment.DURABILITY,1, true)
            }
        }
    }
}