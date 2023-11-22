package kor.toxicity.questadder.api.event;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerDropQuestItemEvent extends PlayerDropItemEvent implements QuestAdderEvent {
    public PlayerDropQuestItemEvent(@NotNull Player player, @NotNull Item drop) {
        super(player, drop);
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
