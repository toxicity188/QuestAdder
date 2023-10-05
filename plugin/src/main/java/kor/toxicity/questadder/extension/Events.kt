package kor.toxicity.questadder.extension

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

fun Event.call(): Boolean {
    Bukkit.getPluginManager().callEvent(this)
    return if (this is Cancellable) !isCancelled else true
}
