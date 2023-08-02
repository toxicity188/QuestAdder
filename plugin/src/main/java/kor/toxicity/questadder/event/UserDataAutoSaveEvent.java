package kor.toxicity.questadder.event;

import kor.toxicity.questadder.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class UserDataAutoSaveEvent extends QuestAdderPlayerEvent {
    private final PlayerData data;
    public UserDataAutoSaveEvent(Player who, PlayerData data) {
        super(who);
        this.data = data;
    }

    public PlayerData getData() {
        return data;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
