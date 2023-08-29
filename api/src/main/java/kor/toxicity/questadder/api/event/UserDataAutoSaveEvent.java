package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.util.IPlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class UserDataAutoSaveEvent extends QuestAdderPlayerEvent {
    private final IPlayerData data;
    public UserDataAutoSaveEvent(Player who, IPlayerData data) {
        super(who);
        this.data = data;
    }

    public IPlayerData getData() {
        return data;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
