package kor.toxicity.questadder

import kor.toxicity.questadder.api.APIManager
import kor.toxicity.questadder.api.item.ItemDatabase
import kor.toxicity.questadder.util.NamedLocation
import kor.toxicity.questadder.api.mechanic.AbstractAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.api.mechanic.AbstractEvent
import kor.toxicity.questadder.api.mechanic.DialogSender
import kor.toxicity.questadder.api.registry.IBlockRegistry
import kor.toxicity.questadder.api.util.INamedLocation
import kor.toxicity.questadder.manager.*
import org.bukkit.entity.Player
import java.util.UUID
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * @author Toxicity
 */
@Suppress("UNUSED")
object QuestAdderAPIBukkit: APIManager {
    /**
     * Add the item database of QuestAdder.
     * @param itemDatabase a database of item.
     * @since 1.0
     */
    override fun addItemDatabase(itemDatabase: ItemDatabase) {
        ItemManager.addItemDatabase(itemDatabase)
    }

    /**
     * Reload this plugin asynchronously.
     * @param callback the function that will be called when reload task is end
     * @since 1.0
     */
    override fun reloadPlugin(callback: Consumer<Long>) {
        QuestAdderBukkit.reload {
            callback.accept(it)
        }
    }

    /**
     * Reload this plugin synchronously.
     * This method doesn't call both ReloadStartEvent and ReloadEndEvent.
     * @since 1.0
     */
    override fun reloadPluginSync() {
        QuestAdderBukkit.reloadSync()
    }

    /**
     * Get the dialog.
     * @since 1.0
     * @param name The yaml key of the dialog
     * @return The object of dialog or null
     */
    override fun getDialog(name: String) = DialogManager.getDialog(name)

    /**
     * Get the quest.
     * @since 1.0
     * @param name The yaml key of the quest
     * @return The object of dialog or null
     */
    override fun getQuest(name: String) = DialogManager.getQuest(name)
    /**
     * Get the action.
     * @since 1.0
     * @param name The yaml key of the action
     * @return the object of action or null
     */
    override fun getAction(name: String) = DialogManager.getAction(name)
    /**
     * Get the location.
     * @since 1.0
     * @param name The yaml key of the location
     * @return The object of location or null
     */
    override fun getLocation(name: String) = LocationManager.getLocation(name)

    /**
     * Add function in QuestAdder.
     * @param name The function name
     * @param args The argument of that function
     * @param function The block of that function
     * @since 1.0
     */

    override fun <T, R> addFunction(
        name: String,
        args: MutableList<Class<*>>,
        argsClass: Class<T>,
        returnClass: Class<R>,
        function: BiFunction<T, Array<Any>, R>
    ) {
        FunctionBuilder.addFunction(name, argsClass, returnClass, args) { t, a ->
            function.apply(t,a)
        }
    }

    /**
     * Get NPC object from given uuid.
     *
     * @param uuid An entity uuid of NPC
     * @return An object of NPC of null
     * @since 1.0.3
     */
    override fun getNPC(uuid: UUID) = DialogManager.getNPC(uuid)
    /**
     * Get NPC object from given uuid.
     *
     * @param name A yaml key of the NPC
     * @return An object of NPC of null
     * @since 1.0.5
     */
    override fun getNPC(name: String) = DialogManager.getNPC(name)

    /**
     * Get all NPC in server.
     *
     * @return All object of NPC in server
     * @since 1.0.3
     */
    override fun getAllNPC() = DialogManager.getAllNPC()

    /**
     * Add action in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    override fun addAction(name: String, clazz: Class<out AbstractAction>) {
        ActionBuilder.addAction(name, clazz)
    }
    /**
     * Add event in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    override fun addEvent(name: String, clazz: Class<out AbstractEvent<*>>) {
        ActionBuilder.addEvent(name, clazz)
    }

    /**
     * Start navigate to specific location.
     * @param player Player
     * @param location Location to navigate
     * @since 1.0.3
     */
    override fun startNavigate(player: Player, location: INamedLocation) {
        NavigationManager.startNavigate(player, location)
    }
    /**
     * End navigate.
     * @param player Player
     * @since 1.0.3
     */
    override fun endNavigate(player: Player) {
        NavigationManager.endNavigate(player)
    }

    /**
     * @return A keys of all of loaded dialogs.
     * @since 1.0.7
     */
    override fun getDialogKeys() = DialogManager.getDialogKeys()
    /**
     * @return A keys of all of loaded quests.
     * @since 1.0.7
     */
    override fun getQuestKeys() = DialogManager.getQuestKeys()
    /**
     * @return A keys of all of loaded QnAs.
     * @since 1.0.7
     */
    override fun getQnaKeys() = DialogManager.getQnAKeys()
    /**
     * @return A keys of all of loaded Actions.
     * @since 1.0.7
     */
    override fun getActionKeys() = DialogManager.getActionKeys()
    /**
     * @return A keys of all of loaded NPCs.
     * @since 1.0.7
     */
    override fun getNPCKeys() = DialogManager.getNPCKeys()
    /**
     * @return A keys of all of loaded quest NPCs.
     * @since 1.0.7
     */
    override fun getQuestNPCKeys() = DialogManager.getQuestNPCKeys()
    override fun getBlockRegistry(): IBlockRegistry {
        return ResourcePackManager.blockRegistry
    }

    override fun isSlated(player: Player): Boolean {
        return SlateManager.isSlated(player)
    }

    override fun getDialogSender(name: String): DialogSender? {
        return DialogManager.getDialogSender(name)
    }
}