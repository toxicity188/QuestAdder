package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.ActionInvokeEvent;
import kor.toxicity.questadder.api.util.DataObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAction implements DataObject, IAction {


    protected final QuestAdder adder;
    public AbstractAction(@NotNull QuestAdder adder) {
        this.adder = adder;
    }

    @NotNull
    public ActionResult apply(@NotNull Player player, @NotNull String... args) {
        var event = new ActionInvokeEvent(this,player,args);
        event.callEvent();
        if (!event.isCancelled()) {
            return invoke(player,event);
        } else {
            return ActionResult.CANCELLED;
        }
    }
    @Override
    public void initialize() {
    }

    public long getRunningTime() {
        return 0;
    }

    public boolean isUnsafe() {
        return true;
    }
}
