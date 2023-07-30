package kor.toxicity.questadder

import kor.toxicity.questadder.item.ItemDatabase
import kor.toxicity.questadder.manager.ItemManager
import kor.toxicity.questadder.util.function.FunctionBuilder

object QuestAdderAPI {
    /**
     * Set the item database of this plugin.
     */
    fun changeItemDatabase(itemDatabase: ItemDatabase) {
        ItemManager.itemDatabase = itemDatabase
    }

    /**
     * Reload this plugin.
     */
    fun reloadPlugin(callback: (Long) -> Unit) {
        QuestAdder.reload(callback)
    }

    /**
     * Add function in QuestAdder.
     */
    inline fun <reified T, reified R> addFunction(name: String, args: List<Class<*>> = emptyList(), noinline function: (t: T, Array<Any>) -> R?) {
        FunctionBuilder.addFunction(name, T::class.java, R::class.java, args, function)
    }
}