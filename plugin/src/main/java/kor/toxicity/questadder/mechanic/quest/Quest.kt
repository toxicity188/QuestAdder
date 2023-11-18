package kor.toxicity.questadder.mechanic.quest

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.api.QuestAdder
import kor.toxicity.questadder.api.event.*
import kor.toxicity.questadder.api.mechanic.IQuest
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.LocationManager
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.ItemWriter
import kor.toxicity.questadder.util.RewardSet
import kor.toxicity.questadder.api.mechanic.AbstractAction
import kor.toxicity.questadder.api.mechanic.ActionResult
import kor.toxicity.questadder.api.mechanic.QuestRecord
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
import java.util.SortedSet
import java.util.TreeSet

class Quest(adder: QuestAdder, val file: File, val questKey: String, section: ConfigurationSection): IQuest, Comparable<Quest> {
    companion object {
        private val success = "Success!".asComponent(YELLOW).clear().decorate(TextDecoration.BOLD)
    }

    private val questName = (section.findString("name","Name") ?: questKey).replace('&','§')

    private val reward = section.findConfig("Reward","reward","Rewards","rewards")?.let {
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
            try {
                Material.valueOf(s.uppercase())
            } catch (ex: Exception) {
                QuestAdderBukkit.warn("not found error: the material named \"$s\" doesn't exist.")
                null
            }
        } ?: Material.BOOK).apply {
            itemMeta = itemMeta?.apply {
                setCustomModelData(it.findInt(0,"custom-model-data","CustomModelData"))
            }
        }
    }
    private val condition = ArrayList<Pair<ComponentReader<QuestInvokeEvent>,WrappedFunction>>()
    private val priority = section.findInt(-1,"priority","Priority")

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
                        QuestAdderBukkit.warn("syntax error: the condition must be have lore.")
                        return@forEach
                    }
                    val s2 = config.findString("condition","Condition") ?: run {
                        QuestAdderBukkit.warn("syntax error: the condition not found.")
                        return@forEach
                    }
                    condition.add(ComponentReader<QuestInvokeEvent>(s1) to FunctionBuilder.evaluate(s2))
                } ?: QuestAdderBukkit.warn("syntax error: the value $s is not configuration section. ($questKey in ${file.name})")
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
                        QuestAdderBukkit.warn("syntax error: the variable must be named. ($questKey in ${file.name})")
                        return@forEach
                    }
                    val lore = config.findString("lore","Lore")?.let { s ->
                        ComponentReader<QuestInvokeEvent>(s)
                    }
                    val event = config.findStringList("Event","event","Events","events") ?: run {
                        QuestAdderBukkit.warn("syntax error: the variable must be have some event. ($questKey in ${file.name})")
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
                        override fun invoke(player: Player, event: QuestAdderEvent): ActionResult {
                            val questEvent = QuestInvokeEvent(
                                this@Quest,
                                player
                            ).apply {
                                call()
                            }
                            if (conditions.all { wf ->
                                    val t = wf.apply(questEvent)
                                    t is Boolean && t
                                }) {
                                val data  = QuestAdderBukkit.getPlayerData(player) ?: return ActionResult.FAIL
                                val newValue = (data.getQuestVariable(questKey,name) ?: 0)
                                if (newValue + 1 == max) {
                                    lore?.createComponent(questEvent)?.let { component ->
                                        QuestAdderBukkit.nms.sendAdvancementMessage(player,toast ?: item.write(questEvent),component)
                                    }
                                    action?.invoke(player,questEvent)
                                }
                                data.setQuestVariable(questKey,name,(newValue + 1).coerceAtMost(max))
                            }
                            return ActionResult.SUCCESS
                        }
                    }
                    for (s in event) {
                        ActionBuilder.createEvent(adder,obj,s)
                    }

                } ?: QuestAdderBukkit.warn("syntax error: the value $it is not configuration section. ($questKey in ${file.name})")
            }
        }
    }
    override fun give(player: Player) {
        QuestAdderBukkit.getPlayerData(player)?.giveQuest(questKey)
        onGive(QuestGiveEvent(this, player).apply {
            call()
        })
    }
    override fun remove(player: Player) {
        QuestAdderBukkit.getPlayerData(player)?.removeQuest(questKey)
        onRemove(QuestRemoveEvent(this, player).apply {
            call()
        })
    }
    override fun complete(player: Player) {
        val reward = reward?.give(player)
        QuestAdderBukkit.getPlayerData(player)?.completeQuest(questKey)
        onComplete(
            QuestCompleteEvent(
                this,
                player,
                reward?.money ?: 0.0,
                reward?.exp ?: 0.0,
                reward?.itemStacks ?: emptyList()
            ).apply {
            call()
        })
    }

    override fun isCompleted(player: Player): Boolean {
        val data = QuestAdderBukkit.getPlayerData(player) ?: return false
        if (data.questVariables[questKey]?.state != QuestRecord.HAS) return false
        val invokeEvent = QuestInvokeEvent(this, player).apply {
            call()
        }
        return condition.isNotEmpty() && condition.all {
            val get = it.second.apply(invokeEvent)
            get is Boolean && get
        }
    }
    override fun isCleared(player: Player) = QuestAdderBukkit.getPlayerData(player)?.questVariables?.get(questKey)?.state == QuestRecord.COMPLETE
    override fun isReady(player: Player) = reward?.isReady(player) ?: true

    fun getState(player: Player) = if (isCleared(player)) QuestState.CLEAR else if (isCompleted(player)) QuestState.COMPLETE else if (has(player)) QuestState.HAS else QuestState.HAS_NOT

    override fun has(player: Player) = QuestAdderBukkit.getPlayerData(player)?.hasQuest(questKey) ?: false
    override fun getIcon(player: Player, suffix: List<Component>): ItemStack {
        val event = QuestInvokeEvent(this, player).apply {
            call()
        }
        val data = QuestAdderBukkit.getPlayerData(player)?.questVariables?.get(questKey)
        val cond = condition.map {
            val get = it.second.apply(event)
            if (get is Boolean) get
            else {
                QuestAdderBukkit.warn("runtime error: the value $get is not a boolean!")
                false
            }
        }
        return item.write(event).apply {
            itemMeta = itemMeta?.also { meta ->
                QuestAdderBukkit.platform.setLore(meta,ArrayList<Component>().apply {
                    addAll(QuestAdderBukkit.platform.getLore(meta))
                    recommend?.let {
                        add(Component.empty())
                        add(QuestAdderBukkit.Prefix.recommend)
                        it.forEach { r ->
                            add(r.createComponent(event) ?: Component.text("error!"))
                        }
                    }
                    if (condition.isNotEmpty()) {
                        add(Component.empty())
                        add(QuestAdderBukkit.Prefix.condition)
                        for ((index,pair) in condition.withIndex()) {
                            val component = pair.first.createComponent(event) ?: Component.empty()

                            add(QuestAdderBukkit.Prefix.conditionLore.append(if (cond[index]) component.deepClear().deepDecorate(TextDecoration.STRIKETHROUGH).append(
                                Component.space().deepClear()).append(success) else component))
                        }
                    }
                    reward?.let {
                        val hasMoney = it.rewardSetMoney >= 0 && !it.hideMoney
                        val hasExp = it.rewardSetExp >= 0  && !it.hideExp
                        val hasItem = it.rewardSetItems.isNotEmpty()
                        if (hasMoney || hasExp || hasItem) {
                            add(Component.empty())
                            add(QuestAdderBukkit.Prefix.reward)
                            if (hasMoney) add(QuestAdderBukkit.Prefix.rewardLore.append(it.rewardSetMoney.withComma().asClearComponent().color(WHITE).append(QuestAdderBukkit.Suffix.money)))
                            if (hasExp) add(QuestAdderBukkit.Prefix.rewardLore.append(it.rewardSetExp.withComma().asClearComponent().color(WHITE).append(QuestAdderBukkit.Suffix.exp)))
                            if (hasItem)  it.rewardSetItems.forEach { i ->
                                var comp = i.contentItem.getNameComponent()
                                if (i.contentChance < 100.0) comp = comp.append(" (${i.contentChance.withComma()}%)".asClearComponent().color(
                                    GRAY))
                                add(QuestAdderBukkit.Prefix.rewardLore.append(comp))
                            }
                        }
                    }
                    if (left > 0 && data != null) {
                        add(Component.empty())
                        add(QuestAdderBukkit.Prefix.left.append(QuestAdderBukkit.Config.timeFormat.format(left - ChronoUnit.MINUTES.between(data.time,LocalDateTime.now())).asClearComponent().color(
                            WHITE)))
                    }
                    addAll(suffix)
                })
                if (cond.isNotEmpty() && cond.all {
                    it
                    }) meta.addEnchant(Enchantment.DURABILITY,1, true)
            }
        }
    }

    override fun toString(): String {
        return questName
    }

    override fun getKey(): String {
        return questKey
    }

    override fun getName(): String {
        return questName
    }

    override fun getTypes(): SortedSet<String> {
        return TreeSet(type)
    }

    override fun getTime(player: Player): LocalDateTime? {
        return QuestAdderBukkit.getPlayerData(player)?.questVariables?.get(questKey)?.time
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quest

        return questKey == other.questKey
    }

    override fun hashCode(): Int {
        return questKey.hashCode()
    }

    override fun compareTo(other: Quest): Int {
        if (priority >= 0 && other.priority >= 0) return priority.compareTo(other.priority)
        return questKey.compareTo(other.questKey)
    }

}
