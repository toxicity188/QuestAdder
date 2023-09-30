package kor.toxicity.questadder.api;

import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface QuestAdder {
    /**
     * Get an instance of QuestAdder's bukkit plugin.
     * This instance can cast as Plugin.
     * @see org.bukkit.plugin.Plugin
     * @return an instance of bukkit plugin.
     * @since 1.1.2
     */
    @NotNull QuestAdderPlugin getPlugin();

    /**
     * Get an API manager of QuestAdder.
     * @return An API manager
     */
    @NotNull APIManager getAPIManager();

    //That methods are only available in AbstractAction and AbstractEvent.
    @ApiStatus.Internal
    void addLazyTask(@NotNull Runnable runnable);
    @ApiStatus.Internal
    <T extends Event> void registerEvent(AbstractEvent<T> event, Class<T> clazz);
}
