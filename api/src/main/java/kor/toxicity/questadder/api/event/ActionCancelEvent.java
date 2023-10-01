package kor.toxicity.questadder.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ActionCancelEvent extends QuestAdderPlayerEvent {
    private final CancelReason reason;
    public ActionCancelEvent(@NotNull Player who, @NotNull CancelReason reason) {
        super(who);
        this.reason = reason;
    }

    public CancelReason getReason() {
        return reason;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public enum CancelReason {
        QUIT,
        DEATH
    }
}
