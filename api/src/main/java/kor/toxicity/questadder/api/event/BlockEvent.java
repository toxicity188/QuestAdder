package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.block.IQuestBlock;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface BlockEvent {
    @NotNull IQuestBlock getQuestBlock();
    static HandlerList getHandlerList() {
        return QuestAdderEvent.HANDLER_LIST;
    }
}
