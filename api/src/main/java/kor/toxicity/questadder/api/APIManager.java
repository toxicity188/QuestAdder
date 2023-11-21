package kor.toxicity.questadder.api;

import kor.toxicity.questadder.api.item.ItemDatabase;
import kor.toxicity.questadder.api.mechanic.*;
import kor.toxicity.questadder.api.registry.IBlockRegistry;
import kor.toxicity.questadder.api.shop.IShop;
import kor.toxicity.questadder.api.util.INamedLocation;
import kor.toxicity.questadder.api.util.IPlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
@SuppressWarnings("unused")
public interface APIManager {
    /**
     * Adds the item database of QuestAdder.
     * @param itemDatabase a database of item.
     * @since 1.0
     */
    void addItemDatabase(@NotNull ItemDatabase itemDatabase);

    /**
     * Reloads this plugin asynchronously.
     * @param callback the function that will be called when reload task is end
     * @since 1.0
     */
    void reloadPlugin(@NotNull Consumer<@NotNull Long> callback);
    /**
     * Reloads this plugin synchronously.
     * This method doesn't call both ReloadStartEvent and ReloadEndEvent.
     * @since 1.0
     */
    void reloadPluginSync();

    /**
     * Gets the dialog.
     * @since 1.0
     * @param name The yaml key of the dialog
     * @return The object of dialog or null
     */
    @Nullable
    IDialog getDialog(@NotNull String name);

    /**
     * Gets the quest.
     * @since 1.0
     * @param name The yaml key of the quest
     * @return The object of dialog or null
     */
    @Nullable
    IQuest getQuest(@NotNull String name);
    /**
     * Gets the action.
     * @since 1.0
     * @param name The yaml key of the action
     * @return the object of action or null
     */
    @Nullable
    AbstractAction getAction(@NotNull String name);
    /**
     * Gets the location.
     * @since 1.0
     * @param name The yaml key of the location
     * @return The object of location or null
     */
    @Nullable
    INamedLocation getLocation(@NotNull String name);

    /**
     * Adds function in QuestAdder.
     * @param name The function name
     * @param args The argument of that function
     * @param function The block of that function
     * @since 1.0
     */
    <T,R> void addFunction(@NotNull String name, @NotNull List<@NotNull Class<?>> args, @NotNull Class<T> argsClass, @NotNull Class<R> returnClass, @NotNull BiFunction<@NotNull T, @NotNull Object[], @NotNull R> function);

    /**
     * Gets NPC object from given uuid.
     *
     * @param uuid An entity uuid of NPC
     * @return An object of NPC of null
     * @since 1.0.3
     */
    @Nullable
    IActualNPC getNPC(@NotNull UUID uuid);
    /**
     * Gets NPC object from given name.
     *
     * @param name A yaml key of the NPC
     * @return An object of NPC of null
     * @since 1.0.5
     */
    @Nullable
    IActualNPC getNPC(@NotNull String name);

    /**
     * Gets all NPC in server.
     *
     * @return All object of NPC in server
     * @since 1.0.3
     */
    @NotNull
    Set<@NotNull IActualNPC> getAllNPC();

    /**
     * Adds action in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    void addAction(@NotNull String name, @NotNull Class<? extends @NotNull AbstractAction> clazz);
    /**
     * Adds event in QuestAdder.
     * @param clazz The class of action
     * @since 1.0.2
     */
    void addEvent(@NotNull String name, @NotNull Class<? extends @NotNull AbstractEvent<?>> clazz);

    /**
     * Starts navigate to specific location.
     * @param player Player
     * @param location Location to navigate
     * @since 1.0.3
     */
    void startNavigate(@NotNull Player player, @NotNull INamedLocation location);
    /**
     * Ends navigate.
     * @param player Player
     * @since 1.0.3
     */
    void endNavigate(@NotNull Player player);

    /**
     * @return A keys of all of loaded dialogs.
     * @since 1.0.7
     */
    @NotNull List<@NotNull String> getDialogKeys();
    /**
     * @return A keys of all of loaded quests.
     * @since 1.0.7
     */
    @NotNull List<@NotNull String> getQuestKeys();
    /**
     * @return A keys of all of loaded QnAs.
     * @since 1.0.7
     */
    @NotNull List<@NotNull String> getQnaKeys();
    /**
     * @return A keys of all of loaded Actions.
     * @since 1.0.7
     */
    @NotNull List<@NotNull String> getActionKeys();
    /**
     * @return A keys of all of loaded NPCs.
     * @since 1.0.7
     */
    @NotNull List<@NotNull String> getNPCKeys();
    /**
     * @return A keys of all of loaded quest NPCs.
     * @since 1.0.7
     */
    @NotNull List<@NotNull String> getQuestNPCKeys();

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
     * Gets Sender object from given uuid.
     *
     * @param name A yaml key of the NPC
     * @return An object of Sender of null
     * @since 1.1.1
     */
    @Nullable DialogSender getDialogSender(@NotNull String name);

    /**
     * Gets the shop.
     * @param name the name of shop
     * @return An object of shop or null if not exist
     * @since 1.1.4
     */
    @Nullable
    IShop getShop(@NotNull String name);

    /**
     * @return A keys of all of loaded shops.
     * @since 1.1.4
     */
    @NotNull List<@NotNull String> getShopKey();

    /**
     * Gets an item from QuestAdder's item database.
     * @param format a string format of item
     * @return A copy of item or null if given format is invalid or id doesn't exist.
     * @since 1.1.7
     */

    @Nullable ItemStack getItem(@NotNull String format);
    /**
     * Generate new player data from given config.
     * @param section An original data of player data.
     * @return A player data
     * @throws Exception A runtime exception if config format is invalid
     * @since 1.1.8
     */
    @NotNull IPlayerData createPlayerData(@NotNull ConfigurationSection section) throws Exception;

    /**
     * Gets a player data from online player's instance.
     * @param player target player
     * @return A data of given player or null if target player is offline
     * @since 1.1.9
     * @see IPlayerData
     */
    @Nullable IPlayerData getPlayerData(@NotNull Player player);

    /**
     * Gets or loads the player data from offline player's instance.
     * @param player target player
     * @param playerDataConsumer a player data consumer
     * @since 1.19.
     * @see IPlayerData
     */
    void getPlayerDataAsync(@NotNull OfflinePlayer player, @NotNull Consumer<@NotNull IPlayerData> playerDataConsumer);
}
