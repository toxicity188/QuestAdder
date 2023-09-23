package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.block.IQuestBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class QuestBlockInteractEvent extends QuestAdderEvent implements Cancellable, BlockEvent {
    private boolean cancelled;
    private final IQuestBlock block;
    private final PlayerInteractEvent event;
    public QuestBlockInteractEvent(IQuestBlock block, PlayerInteractEvent event) {
        this.block = block;
        this.event = event;
    }

    public PlayerInteractEvent getEvent() {
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
