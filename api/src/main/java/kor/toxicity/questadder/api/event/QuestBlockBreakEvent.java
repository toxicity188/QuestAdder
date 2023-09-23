package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.block.IQuestBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class QuestBlockBreakEvent extends QuestAdderEvent implements Cancellable, BlockEvent {
    private boolean cancelled;
    private final IQuestBlock block;
    private final BlockBreakEvent event;
    public QuestBlockBreakEvent(IQuestBlock block, BlockBreakEvent event) {
        this.block = block;
        this.event = event;
    }

    public BlockBreakEvent getEvent() {
        return event;
    }

    @NotNull
    @Override
    public IQuestBlock getQuestBlock() {
        return block;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
