package kor.toxicity.questadder.api.event;

import org.bukkit.event.HandlerList;

public class ReloadEndEvent extends QuestAdderEvent {
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
