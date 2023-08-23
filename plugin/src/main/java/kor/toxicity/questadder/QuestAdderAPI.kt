package kor.toxicity.questadder

import kor.toxicity.questadder.item.ItemDatabase
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.manager.ItemManager
import kor.toxicity.questadder.manager.LocationManager
import kor.toxicity.questadder.util.action.AbstractAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.event.AbstractEvent

/**
 * @author Toxicity
 */
object QuestAdderAPI {
    /**
     * Set the item database of QuestAdder.
     * @since 1.0
     */
    fun addItemDatabase(itemDatabase: ItemDatabase) {
        ItemManager.addItemDatabase(itemDatabase)
    }

    /**
     * Reload this plugin asynchronously.
     * @param callback the function that will be called when reload task is end
     * @since 1.0
     */
    fun reloadPlugin(callback: (Long) -> Unit) {
        QuestAdder.reload(callback)
    }

    /**
     * Reload this plugin synchronously.
     * This method doesn't call both ReloadStartEvent and ReloadEndEvent.
     * @since 1.0
     */
    fun reloadPluginSync() {
        QuestAdder.reloadSync()
    }

    /**
     * Get the dialog.
     * @since 1.0
     * @param name The yaml key of dialog
     * @return the object of dialog or null
     */
    fun getDialog(name: String) = DialogManager.getDialog(name)

    /**
     * Get the quest.
     * @since 1.0
     * @param name The yaml key of quest
     * @return the object of dialog or null
     */
    fun getQuest(name: String) = DialogManager.getQuest(name)
    /**
     * Get the action.
     * @since 1.0
     * @param name The yaml key of action
     * @return the object of action or null
     */
    fun getAction(name: String) = DialogManager.getAction(name)
    /**
     * Get the location.
     * @since 1.0
     * @param name The yaml key of location
     * @return the object of location or null
     */
    fun getLocation(name: String) = LocationManager.getLocation(name)

    /**
     * Add function in QuestAdder.
     * @param name The function name
     * @param args The argument of that function
     * @param function The block of that function
     * @since 1.0
     */
    inline fun <reified T, reified R> addFunction(name: String, args: List<Class<*>> = emptyList(), noinline function: (t: T, Array<Any>) -> R?) {
        FunctionBuilder.addFunction(name, T::class.java, R::class.java, args, function)
    }

    /**
     * Add action in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    fun addAction(name: String, clazz: Class<out AbstractAction>) {
        ActionBuilder.addAction(name, clazz)
    }
    /**
     * Add event in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    fun addEvent(name: String, clazz: Class<out AbstractEvent<*>>) {
        ActionBuilder.addEvent(name, clazz)
    }
}