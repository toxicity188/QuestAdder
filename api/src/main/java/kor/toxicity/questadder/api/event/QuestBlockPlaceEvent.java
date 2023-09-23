package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.block.IQuestBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class QuestBlockPlaceEvent extends QuestAdderEvent implements Cancellable, BlockEvent {
    private boolean cancelled;
    private final IQuestBlock block;
    private final BlockPlaceEvent event;
    public QuestBlockPlaceEvent(IQuestBlock block, BlockPlaceEvent event) {
        this.block = block;
        this.event = event;
    }

    public BlockPlaceEvent getEvent() {
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
