package kor.toxicity.questadder.api;

import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface QuestAdder {
    @NotNull QuestAdderPlugin getPlugin();
    @NotNull APIManager getAPIManager();

    @ApiStatus.Internal
    void addLazyTask(@NotNull Runnable runnable);
    @ApiStatus.Internal
    <T extends Event> void registerEvent(AbstractEvent<T> event, Class<T> clazz);
}
