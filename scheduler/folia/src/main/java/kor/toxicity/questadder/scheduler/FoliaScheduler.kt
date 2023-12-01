package kor.toxicity.questadder.scheduler

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class FoliaScheduler: QuestAdderScheduler {
    override fun task(plugin: Plugin, location: Location?, action: () -> Unit): ScheduledTask {
        val task = if (location != null) Bukkit.getRegionScheduler().run(plugin, location) {
            action()
        } else Bukkit.getGlobalRegionScheduler().run(plugin) {
            action()
        }
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTask(plugin: Plugin, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getAsyncScheduler().runNow(plugin) {
            action()
        }
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun taskLater(plugin: Plugin, location: Location?, delay: Long, action: () -> Unit): ScheduledTask {
        val task = if (location != null) Bukkit.getRegionScheduler().runDelayed(plugin, location, {
            action()
        }, delay) else Bukkit.getGlobalRegionScheduler().runDelayed(plugin, {
            action()
        }, delay)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTaskLater(plugin: Plugin, delay: Long, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getAsyncScheduler().runDelayed(plugin, {
            action()
        }, delay * 50, TimeUnit.MILLISECONDS)
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
        val task = if (location != null) Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, {
            action()
        }, delay, period) else Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, {
            action()
        }, delay, period)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun asyncTaskTimer(plugin: Plugin, delay: Long, period: Long, action: () -> Unit): ScheduledTask {
        val task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
            action()
        }, delay * 50, period * 50, TimeUnit.MILLISECONDS)
        return object : ScheduledTask {
            override fun cancel() {
                task.cancel()
            }
        }
    }

    override fun teleport(entity: Entity, location: Location) {
        entity.teleportAsync(location)
    }
}
