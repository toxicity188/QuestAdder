package kor.toxicity.questadder.api.event;

import io.th0rgal.oraxen.shaded.jetbrains.annotations.NotNull;
import kor.toxicity.questadder.api.mechanic.IAction;
import org.bukkit.event.HandlerList;

public interface ActionEvent {
    @NotNull IAction getAction();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
