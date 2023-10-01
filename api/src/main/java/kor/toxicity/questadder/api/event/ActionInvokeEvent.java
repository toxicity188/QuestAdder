package kor.toxicity.questadder.api.event;

import io.th0rgal.oraxen.shaded.jetbrains.annotations.NotNull;
import kor.toxicity.questadder.api.mechanic.IAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ActionInvokeEvent extends QuestAdderPlayerEvent implements Cancellable, ActionEvent {

    private final IAction action;
    private final String[] args;
    public ActionInvokeEvent(IAction action, Player who, String... args) {
        super(who);
        this.action = action;
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    @NotNull
    public IAction getAction() {
        return action;
    }

    private boolean cancelled;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
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
