package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.util.reflect.DataObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAction implements DataObject {


    protected final QuestAdder adder;
    public AbstractAction(@NotNull QuestAdder adder) {
        this.adder = adder;
    }

    public boolean apply(@NotNull Player player, String... args) {
        var event = new ActionInvokeEvent(this,player,args);
        event.callEvent();
        if (!event.isCancelled()) {
            invoke(player,event);
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void initialize() {
    }
    public abstract void invoke(@NotNull Player player, @NotNull QuestAdderEvent event);
}
