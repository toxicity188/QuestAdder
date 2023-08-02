package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import kotlin.Unit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActAction extends AbstractAction {
    @DataField(aliases = "n",throwIfNull = true)
    public String name;

    public ActAction(@NotNull QuestAdder adder) {
        super(adder);
    }

    private AbstractAction action;
    @Override
    public void initialize() {
        super.initialize();
        adder.addLazyTask(() -> {
            action = DialogManager.INSTANCE.getAction(name);
            if (action == null) QuestAdder.Companion.warn("not found error: the action named \"" + name + "\" doesn't exist.");
            return Unit.INSTANCE;
        });
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull ActionInvokeEvent event) {
        if (action != null) action.invoke(player,event);
    }
}
