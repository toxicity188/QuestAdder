package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IActualNPC;
import kor.toxicity.questadder.api.mechanic.IDialog;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DialogStartEvent extends QuestAdderPlayerEvent implements DialogEvent, NPCEvent, Cancellable {

    private final IActualNPC npc;
    private final IDialog dialog;
    private boolean cancelled;
    public DialogStartEvent(Player who, IActualNPC npc, IDialog dialog) {
        super(who);
        this.npc = npc;
        this.dialog = dialog;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
}

    public @NotNull IDialog getDialog() {
        return dialog;
    }

    public @NotNull IActualNPC getNpc() {
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
