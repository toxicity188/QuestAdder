package kor.toxicity.questadder.api.event;

import org.bukkit.event.HandlerList;

public class ReloadStartEvent extends QuestAdderEvent {
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
