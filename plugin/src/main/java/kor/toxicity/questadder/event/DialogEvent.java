package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.Dialog;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface DialogEvent {
    @NotNull Dialog getDialog();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
