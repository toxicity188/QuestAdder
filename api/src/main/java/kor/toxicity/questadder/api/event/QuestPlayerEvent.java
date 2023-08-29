package kor.toxicity.questadder.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface QuestPlayerEvent extends QuestEvent {
    @NotNull Player getPlayer();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
