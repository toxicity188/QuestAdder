package kor.toxicity.questadder.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public abstract class QuestAdderPlayerEvent extends QuestAdderEvent {
    private final Player player;
    public QuestAdderPlayerEvent(Player who) {
        super();
        player = who;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
