package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.Quest;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface QuestEvent {
    @NotNull Quest getQuest();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
