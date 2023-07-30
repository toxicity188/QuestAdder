package kor.toxicity.questadder.event;

import kor.toxicity.questadder.mechanic.Dialog;
import kor.toxicity.questadder.mechanic.QuestNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class DialogStartEvent extends QuestAdderPlayerEvent implements DialogEvent, NPCEvent, Cancellable {

    private final QuestNPC npc;
    private final Dialog dialog;
    private boolean cancelled;
    public DialogStartEvent(Player who, QuestNPC npc, Dialog dialog) {
        super(who);
        this.npc = npc;
        this.dialog = dialog;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public QuestNPC getNpc() {
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
