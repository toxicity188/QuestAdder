package kor.toxicity.questadder.mechanic.quest

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.event.*
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.LocationManager
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.ItemWriter
import kor.toxicity.questadder.util.RewardSet
import kor.toxicity.questadder.util.action.AbstractAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.function.WrappedFunction
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.TreeSet

class Quest(adder: QuestAdder, file: File, val key: String, section: ConfigurationSection) {
    companion object {
        private val success = "Success!".asComponent(YELLOW).clear().decorate(TextDecoration.BOLD)
    }

    val name = (section.findString("name","Name") ?: key).replace('&','§')

    val reward = section.findConfig("Reward","reward","Rewards","rewards")?.let {
        RewardSet(it)
    }
    private val recommend = section.findStringList("recommend")?.map {
        ComponentReader<QuestInvokeEvent>(it)
    }
    val cancellable = section.findBoolean("Cancellable","cancellable")

    val left = section.findLong(0L,"Left","left")

    val type = section.findStringList("Type","type","Types","types")?.toSortedSet() ?: TreeSet()

    private val item = section.findConfig("Item","item","Icon","icon")?.let { c ->
        try {
            ItemWriter<QuestInvokeEvent>(c)
        } catch (ex: Exception) {
            null
        }
    } ?: throw RuntimeException("the quest has no icon.")
    private val toast = section.findConfig("toast")?.let {
        ItemStack(it.findString("type","Type")?.let { s ->
            section.getString("toast")?.let {
                try {
                    Material.valueOf(s.uppercase())
                } catch (ex: Exception) {
                    QuestAdder.warn("not found error: the material named \"$s\" doesn't exist.")
                    null
                }
            }
        } ?: Material.BOOK).apply {
            itemMeta = itemMeta?.apply {
                setCustomModelData(it.findInt(0,"custom-model-data","CustomModelData"))
            }
        }
    }
    private val condition = ArrayList<Pair<ComponentReader<QuestInvokeEvent>,WrappedFunction>>()

    private var onRemove: (QuestAdderPlayerEvent) -> Unit = {}
    private var onComplete: (QuestAdderPlayerEvent) -> Unit = {}
    private var onGive: (QuestAdderPlayerEvent) -> Unit = {}

    val locationList = section.findStringList("location","Location","locations","Locations")?.mapNotNull {s ->
        LocationManager.getLocation(s)
    }

    fun getConditions() = condition.map {
        it.first
    }
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
        section.findStringList("onRemove","on-remove")?.let { c ->
            ActionBuilder.create(adder,c)?.let { act ->
                val before = onRemove
                onRemove = {
                    before(it)
                    act.invoke(it.player,it)
                }
            }
        }
        section.findStringList("onComplete","on-complete")?.let { c ->
            ActionBuilder.create(adder,c)?.let { act ->
                val before = onComplete
                onComplete = {
                    before(it)
                    act.invoke(it.player,it)
                }
            }
        }
        section.findStringList("onGive","on-give")?.let { c ->
            ActionBuilder.create(adder,c)?.let { act ->
                val before = onGive
                onGive = {
                    before(it)
                    act.invoke(it.player,it)
                }
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
                    val action = config.findStringList("Action","Actions","actions","action")?.let { act ->
                        ActionBuilder.create(adder, act)
                    }
                    val conditions = config.findStringList("Conditions","Condition","conditions","condition")?.map {s ->
                        FunctionBuilder.evaluate(s)
                    } ?: emptyList()
                    val max = config.findInt(0,"Max","max").toLong()
                    val obj: AbstractAction = object : AbstractAction(adder) {
                        override fun invoke(player: Player, event: QuestAdderEvent) {
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
                                        QuestAdder.nms.sendAdvancementMessage(player,toast ?: item.write(questEvent),component)
                                    }
                                    action?.invoke(player,questEvent)
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
        onGive(QuestGiveEvent(this,player).apply {
            callEvent()
        })
    }
    fun remove(player: Player) {
        QuestAdder.getPlayerData(player)?.removeQuest(key)
        onRemove(QuestRemoveEvent(this,player).apply {
            callEvent()
        })
    }
    fun complete(player: Player) {
        val reward = reward?.give(player)
        QuestAdder.getPlayerData(player)?.completeQuest(key)
        onComplete(QuestCompleteEvent(this,player,reward?.money ?: 0.0, reward?.exp ?: 0.0, reward?.itemStacks ?: emptyList()).apply {
            callEvent()
        })
    }

    fun isCompleted(player: Player): Boolean {
        val data = QuestAdder.getPlayerData(player) ?: return false
        if (data.questVariables[key]?.state != QuestRecord.HAS) return false
        val invokeEvent = QuestInvokeEvent(this,player).apply {
            callEvent()
        }
        return condition.isNotEmpty() && condition.all {
            val get = it.second.apply(invokeEvent)
            get is Boolean && get
        }
    }
    fun isCleared(player: Player) = QuestAdder.getPlayerData(player)?.questVariables?.get(key)?.state == QuestRecord.COMPLETE
    fun isReady(player: Player) = reward?.isReady(player) ?: true

    fun getState(player: Player) = if (isCleared(player)) QuestState.CLEAR else if (isCompleted(player)) QuestState.COMPLETE else if (has(player)) QuestState.HAS else QuestState.HAS_NOT

    fun has(player: Player) = QuestAdder.getPlayerData(player)?.hasQuest(key) ?: false
    fun getIcon(player: Player, suffix: List<Component> = QuestAdder.Config.questSuffix): ItemStack {
        val event = QuestInvokeEvent(this,player).apply {
            callEvent()
        }
        val data = QuestAdder.getPlayerData(player)?.questVariables?.get(key)
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
                    }
                    recommend?.let {
                        add(Component.empty())
                        add(QuestAdder.Prefix.recommend)
                        it.forEach { r ->
                            add(r.createComponent(event) ?: Component.text("error!"))
                        }
                    }
                    if (condition.isNotEmpty()) {
                        add(Component.empty())
                        add(QuestAdder.Prefix.condition)
                        for ((index,pair) in condition.withIndex()) {
                            val component = pair.first.createComponent(event) ?: Component.empty()

                            add(QuestAdder.Prefix.conditionLore.append(if (cond[index]) component.deepClear().deepDecorate(TextDecoration.STRIKETHROUGH).append(
                                Component.space().deepClear()).append(success) else component))
                        }
                    }
                    reward?.let {
                        add(Component.empty())
                        add(QuestAdder.Prefix.reward)
                        add(QuestAdder.Prefix.rewardLore.append(it.money.withComma().asClearComponent().color(WHITE).append(QuestAdder.Suffix.money)))
                        add(QuestAdder.Prefix.rewardLore.append(it.exp.withComma().asClearComponent().color(WHITE).append(QuestAdder.Suffix.exp)))
                        it.items.forEach { i ->
                            var comp = i.item.getNameComponent()
                            if (i.chance < 100.0) comp = comp.append(" (${i.chance.withComma()}%)".asClearComponent().color(
                                GRAY))
                            add(QuestAdder.Prefix.rewardLore.append(comp))
                        }
                    }
                    if (left > 0 && data != null) {
                        add(Component.empty())
                        add(QuestAdder.Prefix.left.append(QuestAdder.Config.timeFormat.format(left - ChronoUnit.MINUTES.between(data.time,LocalDateTime.now())).asClearComponent().color(
                            WHITE)))
                    }
                    addAll(suffix)
                })
                if (cond.all {
                    it
                    }) addEnchant(Enchantment.DURABILITY,1, true)
            }
        }
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quest

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}