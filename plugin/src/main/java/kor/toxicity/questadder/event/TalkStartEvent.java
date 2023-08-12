package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.Dialog;
import kor.toxicity.questadder.mechanic.npc.ActualNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TalkStartEvent extends QuestAdderPlayerEvent implements Cancellable {
    private boolean cancelled;

    private final Dialog dialog;
    private final ActualNPC npc;
    public TalkStartEvent(@NotNull Player who, @NotNull Dialog dialog, @NotNull ActualNPC npc) {
        super(who);
        this.dialog = dialog;
        this.npc = npc;
    }

    @NotNull
    public Dialog getDialog() {
        return dialog;
    }
    @NotNull
    public ActualNPC getNpc() {
        return npc;
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
