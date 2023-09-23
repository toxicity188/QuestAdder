package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IAction;
import org.bukkit.event.HandlerList;

public interface ActionEvent {
    IAction getAction();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
