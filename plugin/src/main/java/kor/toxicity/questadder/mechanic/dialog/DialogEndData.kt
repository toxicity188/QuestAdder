package kor.toxicity.questadder.mechanic.dialog

import kor.toxicity.questadder.api.event.DialogStartEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class DialogEndData(val player: Player, val event: DialogStartEvent, val guiName: Component, val talker: Component, val talk: Component?)