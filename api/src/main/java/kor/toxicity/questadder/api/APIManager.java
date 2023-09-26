package kor.toxicity.questadder.api;

import kor.toxicity.questadder.api.item.ItemDatabase;
import kor.toxicity.questadder.api.mechanic.*;
import kor.toxicity.questadder.api.registry.IBlockRegistry;
import kor.toxicity.questadder.api.util.INamedLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author toxicity
 */
public interface APIManager {
    /**
     * Add the item database of QuestAdder.
     * @param itemDatabase a database of item.
     * @since 1.0
     */
    void addItemDatabase(@NotNull ItemDatabase itemDatabase);

    /**
     * Reload this plugin asynchronously.
     * @param callback the function that will be called when reload task is end
     * @since 1.0
     */
    void reloadPlugin(@NotNull Consumer<Long> callback);
    /**
     * Reload this plugin synchronously.
     * This method doesn't call both ReloadStartEvent and ReloadEndEvent.
     * @since 1.0
     */
    void reloadPluginSync();

    /**
     * Get the dialog.
     * @since 1.0
     * @param name The yaml key of the dialog
     * @return The object of dialog or null
     */
    @Nullable
    IDialog getDialog(@NotNull String name);

    /**
     * Get the quest.
     * @since 1.0
     * @param name The yaml key of the quest
     * @return The object of dialog or null
     */
    @Nullable
    IQuest getQuest(@NotNull String name);
    /**
     * Get the action.
     * @since 1.0
     * @param name The yaml key of the action
     * @return the object of action or null
     */
    @Nullable
    AbstractAction getAction(@NotNull String name);
    /**
     * Get the location.
     * @since 1.0
     * @param name The yaml key of the location
     * @return The object of location or null
     */
    @Nullable
    INamedLocation getLocation(@NotNull String name);

    /**
     * Add function in QuestAdder.
     * @param name The function name
     * @param args The argument of that function
     * @param function The block of that function
     * @since 1.0
     */
    <T,R> void addFunction(@NotNull String name, @NotNull List<Class<?>> args, @NotNull Class<T> argsClass, @NotNull Class<R> returnClass, @NotNull BiFunction<T,Object[],R> function);

    /**
     * Get NPC object from given uuid.
     *
     * @param uuid An entity uuid of NPC
     * @return An object of NPC of null
     * @since 1.0.3
     */
    @Nullable
    IActualNPC getNPC(@NotNull UUID uuid);
    /**
     * Get NPC object from given name.
     *
     * @param name A yaml key of the NPC
     * @return An object of NPC of null
     * @since 1.0.5
     */
    @Nullable
    IActualNPC getNPC(@NotNull String name);

    /**
     * Get all NPC in server.
     *
     * @return All object of NPC in server
     * @since 1.0.3
     */
    @NotNull
    Set<IActualNPC> getAllNPC();

    /**
     * Add action in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    void addAction(@NotNull String name, @NotNull Class<? extends AbstractAction> clazz);
    /**
     * Add event in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    void addEvent(@NotNull String name, @NotNull Class<? extends AbstractEvent<?>> clazz);

    /**
     * Start navigate to specific location.
     * @param player Player
     * @param location Location to navigate
     * @since 1.0.3
     */
    void startNavigate(@NotNull Player player, @NotNull INamedLocation location);
    /**
     * End navigate.
     * @param player Player
     * @since 1.0.3
     */
    void endNavigate(@NotNull Player player);

    /**
     * @return A keys of all of loaded dialogs.
     * @since 1.0.7
     */
    @NotNull List<String> getDialogKeys();
    /**
     * @return A keys of all of loaded quests.
     * @since 1.0.7
     */
    @NotNull List<String> getQuestKeys();
    /**
     * @return A keys of all of loaded QnAs.
     * @since 1.0.7
     */
    @NotNull List<String> getQnaKeys();
    /**
     * @return A keys of all of loaded Actions.
     * @since 1.0.7
     */
    @NotNull List<String> getActionKeys();
    /**
     * @return A keys of all of loaded NPCs.
     * @since 1.0.7
     */
    @NotNull List<String> getNPCKeys();
    /**
     * @return A keys of all of loaded quest NPCs.
     * @since 1.0.7
     */
    @NotNull List<String> getQuestNPCKeys();

    /**
     * @since 1.0.10
     * @return a block registry of QuestAdder
     */
    @NotNull
    IBlockRegistry getBlockRegistry();

    /**
     * @since 1.1.0
     * @param player target player
     * @return whether player is on slate
     */
    boolean isSlated(@NotNull Player player);

    /**
     * Get Sender object from given uuid.
     *
     * @param name A yaml key of the NPC
     * @return An object of Sender of null
     * @since 1.1.1
     */
    @Nullable DialogSender getDialogSender(@NotNull String name);
}
