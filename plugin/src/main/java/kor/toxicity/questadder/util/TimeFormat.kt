package kor.toxicity.questadder.util

import org.bukkit.configuration.ConfigurationSection

class TimeFormat(section: ConfigurationSection) {
    private val dayFormat = section.getString("day") ?: "%dd"
    private val hourFormat = section.getString("hour") ?: "%dh"
    private val minuteFormat = section.getString("minute") ?: "%dm"

    fun format(long: Long): String {
        val builder = StringBuilder()
        val t = long / 60
        val day = t / 24
        val hour = t % 24
        val minute = long % 60
        if (day > 0) builder.append(String.format(dayFormat,day))
        if (hour > 0) {
            if (builder.isNotEmpty()) builder.append(' ')
            builder.append(String.format(hourFormat,hour))
        }
        if (builder.isNotEmpty()) builder.append(' ')
        builder.append(String.format(minuteFormat,minute))
        return builder.toString()
    }
}