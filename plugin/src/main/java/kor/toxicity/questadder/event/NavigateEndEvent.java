package kor.toxicity.questadder.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NavigateEndEvent extends QuestAdderPlayerEvent {
    public NavigateEndEvent(@NotNull Player who) {
        super(who);
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
