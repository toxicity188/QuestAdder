package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.npc.ActualNPC;
import kor.toxicity.questadder.mechanic.Dialog;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DialogStartEvent extends QuestAdderPlayerEvent implements DialogEvent, NPCEvent, Cancellable {

    private final ActualNPC npc;
    private final Dialog dialog;
    private boolean cancelled;
    public DialogStartEvent(Player who, ActualNPC npc, Dialog dialog) {
        super(who);
        this.npc = npc;
        this.dialog = dialog;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
}

    public @NotNull Dialog getDialog() {
        return dialog;
    }

    public @NotNull ActualNPC getNpc() {
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
}
