package kor.toxicity.questadder.scheduler

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

interface QuestAdderScheduler {
    fun task(plugin: Plugin, location: Location?, action: () -> Unit): ScheduledTask
    fun asyncTask(plugin: Plugin, action: () -> Unit): ScheduledTask


    fun taskLater(plugin: Plugin, location: Location?, delay: Long, action: () -> Unit): ScheduledTask
    fun asyncTaskLater(plugin: Plugin, delay: Long, action: () -> Unit): ScheduledTask


    fun taskTimer(plugin: Plugin, location: Location?, delay: Long, period: Long, action: () -> Unit): ScheduledTask
    fun asyncTaskTimer(plugin: Plugin, delay: Long, period: Long, action: () -> Unit): ScheduledTask

    fun teleport(entity: Entity, location: Location)
}
