package kor.toxicity.questadder.event;

import kor.toxicity.questadder.util.action.AbstractAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ActionInvokeEvent extends QuestAdderPlayerEvent implements Cancellable, ActionEvent {

    private final AbstractAction action;
    private final String[] args;
    public ActionInvokeEvent(AbstractAction action, Player who, String... args) {
        super(who);
        this.action = action;
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public AbstractAction getAction() {
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
