package kor.toxicity.questadder.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ButtonGuiOpenEvent extends QuestAdderPlayerEvent implements Cancellable {

    private boolean cancelled;

    public ButtonGuiOpenEvent(@NotNull Player who) {
        super(who);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
