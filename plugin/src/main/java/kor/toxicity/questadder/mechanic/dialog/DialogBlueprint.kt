package kor.toxicity.questadder.mechanic.dialog

import kor.toxicity.questadder.api.mechanic.MechanicBlueprint
import kor.toxicity.questadder.extension.copy
import kor.toxicity.questadder.extension.findConfig
import kor.toxicity.questadder.extension.findStringList
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.MemoryConfiguration

class DialogBlueprint(
    var typingSound: ConfigurationSection?,
    var typingSpeed: ConfigurationSection?,
    var talk: Array<String>?,
    var talker: ConfigurationSection?,
    var dialog: Array<String>?,
    var action: ConfigurationSection?,
    var endAction: Array<String>?,
    var gestures: ConfigurationSection?,
    var sounds: ConfigurationSection?,
    var interfaces: ConfigurationSection?,
    var subDialog: Array<String>?,
    var index: Array<String>?,
    var variable: Array<String>?,
    var condition: Array<String>?,
    var setQuest: Array<String>?,
    var checkQuest: Array<String>?,
    var qna: Array<String>?,
    var shops: Array<String>?,
    var takeItem: Array<String>?,
    val giveItem: Array<String>?
): MechanicBlueprint {
    constructor(section: ConfigurationSection): this(
        section.findConfig("TypingSound","typing-sound"),
        section.findConfig("TypingSpeed","typing-speed"),
        section.findStringList("talk","Talk")?.toTypedArray(),
        section.findConfig("talker","Talker","sender","Sender"),
        section.findStringList("dialog","Dialog","LinkedDialog","linked-dialog")?.toTypedArray(),
        section.findConfig("Action","action","actions","Actions"),
        section.findStringList("EndAction","end-action")?.toTypedArray(),
        section.findConfig("Gesture","gesture","gestures","Gestures"),
        section.findConfig("Sound","Sounds","sounds","sound"),
        section.findConfig("Interface","interface"),
        section.findStringList("SubDialog","sub-dialog","LinkedSubDialog","linked-sub-dialog")?.toTypedArray(),
        section.findStringList("index","Index","Indexes","indexes")?.toTypedArray(),
        section.findStringList("set-vars","SetVars","Vars","vars","Variables","variables","variable","Variable")?.toTypedArray(),
        section.findStringList("conditions","Conditions","condition","Condition")?.toTypedArray(),
        section.findStringList("quest","Quest","SetQuest","set-quest")?.toTypedArray(),
        section.findStringList("Check","check","CheckQuest","check-quest")?.toTypedArray(),
        section.findStringList("QnA","QnAs","qna","qnas")?.toTypedArray(),
        section.findStringList("Shop","Shops","shop","shops")?.toTypedArray(),
        section.findStringList("take-item", "TakeItem")?.toTypedArray(),
        section.findStringList("give-item", "GiveItem")?.toTypedArray()
    )

    fun copy() = DialogBlueprint(
        typingSound?.copy(),
        typingSpeed?.copy(),
        talk?.copyOf(),
        talker?.copy(),
        dialog?.copyOf(),
        action?.copy(),
        endAction?.copyOf(),
        gestures?.copy(),
        sounds?.copy(),
        interfaces?.copy(),
        subDialog?.copyOf(),
        index?.copyOf(),
        variable?.copyOf(),
        condition?.copyOf(),
        setQuest?.copyOf(),
        checkQuest?.copyOf(),
        qna?.copyOf(),
        shops?.copyOf(),
        takeItem?.copyOf(),
        giveItem?.copyOf()
    )
    override fun getConfig() = MemoryConfiguration().apply {
        set("talk", talk)
        set("talker", talker)
        set("typing-sound", typingSound)
        set("typing-speed", typingSpeed)
        set("dialog", dialog)
        set("action", action)
        set("end-action", endAction)
        set("gesture", gestures)
        set("sound", sounds)
        set("interface", interfaces)
        set("sub-dialog", subDialog)
        set("index", index)
        set("vars", variable)
        set("condition", condition)
        set("quest", setQuest)
        set("check", checkQuest)
        set("qna", qna)
        set("shop", shops)
        set("take-item", takeItem)
        set("give-item", giveItem)
    }
}
