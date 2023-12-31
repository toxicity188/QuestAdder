package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.ActionInvokeEvent;
import kor.toxicity.questadder.api.util.DataObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class AbstractAction implements DataObject, IAction {


    protected final QuestAdder adder;
    public AbstractAction(@NotNull QuestAdder adder) {
        this.adder = adder;
    }

    @NotNull
    public ActionResult apply(@NotNull Player player, @NotNull String... args) {
        var event = new ActionInvokeEvent(this,player,args);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled() ? invoke(player,event) : ActionResult.CANCELLED;
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
