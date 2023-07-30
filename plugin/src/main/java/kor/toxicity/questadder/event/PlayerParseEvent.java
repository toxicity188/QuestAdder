package kor.toxicity.questadder.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerParseEvent extends QuestAdderPlayerEvent {
    public PlayerParseEvent(Player who) {
        super(who);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
