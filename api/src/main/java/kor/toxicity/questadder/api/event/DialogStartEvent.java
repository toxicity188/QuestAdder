package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.DialogSender;
import kor.toxicity.questadder.api.mechanic.IDialog;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DialogStartEvent extends QuestAdderPlayerEvent implements DialogEvent, Cancellable {

    private final DialogSender sender;
    private final IDialog dialog;
    private boolean cancelled;
    public DialogStartEvent(Player who, DialogSender sender, IDialog dialog) {
        super(who);
        this.sender = sender;
        this.dialog = dialog;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
}

    public @NotNull IDialog getDialog() {
        return dialog;
    }

    public @NotNull DialogSender getSender() {
        return sender;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
