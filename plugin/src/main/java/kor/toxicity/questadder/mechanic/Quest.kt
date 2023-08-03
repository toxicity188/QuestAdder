package kor.toxicity.questadder.mechanic

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.ActionInvokeEvent
import kor.toxicity.questadder.event.QuestGiveEvent
import kor.toxicity.questadder.event.QuestInvokeEvent
import kor.toxicity.questadder.event.QuestRemoveEvent
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.ItemWriter
import kor.toxicity.questadder.util.action.AbstractAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.function.WrappedFunction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class Quest(adder: QuestAdder, file: File, val key: String, section: ConfigurationSection) {
    companion object {
        private val success = "Success!".asComponent(YELLOW).clear().decorate(TextDecoration.BOLD)
    }

    val name = (section.findString("name","Name") ?: key).replace('&','§')

    private val item = section.findConfig("Item","item","Icon","icon")?.let { c ->
        try {
            ItemWriter<QuestInvokeEvent>(c)
        } catch (ex: Exception) {
            null
        }
    } ?: throw RuntimeException("the quest has no icon.")
    private val condition = ArrayList<Pair<ComponentReader<QuestInvokeEvent>,WrappedFunction>>()

    init {
        section.findConfig("Conditions","Condition","conditions","condition")?.let { c ->
            c.getKeys(false).forEach { s ->
                c.getConfigurationSection(s)?.let { config ->
                    val s1 = config.findString("lore","Lore") ?: run {
                        QuestAdder.warn("syntax error: the condition must be have lore.")
                        return@forEach
                    }
                    val s2 = config.findString("condition","Condition") ?: run {
                        QuestAdder.warn("syntax error: the condition not found.")
                        return@forEach
                    }
                    condition.add(ComponentReader<QuestInvokeEvent>(s1) to FunctionBuilder.evaluate(s2))
                } ?: QuestAdder.warn("syntax error: the value $s is not configuration section. ($key in ${file.name})")
            }
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
        QuestGiveEvent(this,player).callEvent()
    }
    fun remove(player: Player) {
        QuestAdder.getPlayerData(player)?.removeQuest(key)
        QuestRemoveEvent(this,player).callEvent()
    }
    fun complete(player: Player) {

    }

    fun isCompleted(player: Player): Boolean {
        val invokeEvent = QuestInvokeEvent(this,player).apply {
            callEvent()
        }
        return condition.all {
            val get = it.second.apply(invokeEvent)
            get is Boolean && get
        }
    }
    fun has(player: Player) = QuestAdder.getPlayerData(player)?.hasQuest(key) ?: false
    fun getIcon(player: Player): ItemStack {
        val event = QuestInvokeEvent(this,player).apply {
            callEvent()
        }
        val cond = condition.map {
            val get = it.second.apply(event)
            if (get is Boolean) get
            else {
                QuestAdder.warn("runtime error: the value $get is not a boolean!")
                false
            }
        }
        return item.write(event).apply {
            itemMeta = itemMeta?.apply {
                lore(ArrayList<Component>().apply {
                    lore()?.let { l ->
                        addAll(l)
                        add(Component.empty())
                    }
                    if (condition.isNotEmpty()) {
                        add(QuestAdder.Prefix.condition)
                        for ((index,pair) in condition.withIndex()) {
                            val component = pair.first.createComponent(event) ?: Component.empty()

                            add(QuestAdder.Prefix.conditionLore.append(if (cond[index]) component.deepClear().deepDecorate(TextDecoration.STRIKETHROUGH).append(
                                Component.space().deepClear()).append(success) else component))
                        }
                    }
                })
                if (cond.all {
                    it
                    }) addEnchant(Enchantment.DURABILITY,1, true)
            }
        }
    }
}