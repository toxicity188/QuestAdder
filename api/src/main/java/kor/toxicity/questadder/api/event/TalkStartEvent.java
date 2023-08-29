package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IActualNPC;
import kor.toxicity.questadder.api.mechanic.IDialog;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TalkStartEvent extends QuestAdderPlayerEvent implements Cancellable {
    private boolean cancelled;

    private final IDialog dialog;
    private final IActualNPC npc;
    public TalkStartEvent(@NotNull Player who, @NotNull IDialog dialog, @NotNull IActualNPC npc) {
        super(who);
        this.dialog = dialog;
        this.npc = npc;
    }

    @NotNull
    public IDialog getDialog() {
        return dialog;
    }
    @NotNull
    public IActualNPC getNpc() {
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
