package kor.toxicity.questadder.nms

import org.bukkit.entity.Item

interface FakeItem {
    fun getItem(): Item
    fun spawn()
}
