package kor.toxicity.questadder.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class QuestAdderPlayerEvent extends QuestAdderEvent {
    private final Player player;
    public QuestAdderPlayerEvent(@NotNull Player who) {
        super();
        player = who;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
