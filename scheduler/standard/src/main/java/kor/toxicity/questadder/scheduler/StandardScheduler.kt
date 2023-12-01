package kor.toxicity.questadder.scheduler

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

class StandardScheduler: QuestAdderScheduler {
    override fun task(plugin: Plugin, location: Location?, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getScheduler().runTask(plugin, action)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTask(plugin: Plugin, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getScheduler().runTaskAsynchronously(plugin, action)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun taskLater(plugin: Plugin, location: Location?, delay: Long, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getScheduler().runTaskLater(plugin, action, delay)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTaskLater(plugin: Plugin, delay: Long, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, action, delay)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun taskTimer(
        plugin: Plugin,
        location: Location?,
        delay: Long,
        period: Long,
        action: () -> Unit
    ): ScheduledTask {
        val task = Bukkit.getScheduler().runTaskTimer(plugin, action, delay, period)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTaskTimer(plugin: Plugin, delay: Long, period: Long, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, action, delay, period)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun teleport(entity: Entity, location: Location) {
        entity.teleport(location)
    }
}
