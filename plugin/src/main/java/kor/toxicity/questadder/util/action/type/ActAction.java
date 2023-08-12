package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.builder.FunctionBuilder;
import kor.toxicity.questadder.util.function.WrappedFunction;
import kor.toxicity.questadder.util.reflect.DataField;
import kotlin.Unit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActAction extends AbstractAction {

    @DataField(aliases = {"if","cond"})
    public String condition;

    @DataField(aliases = "n",throwIfNull = true)
    public String name;

    @DataField(aliases = "i",throwIfNull = true)
    public String instead;
    public ActAction(@NotNull QuestAdder adder) {
        super(adder);
    }

    private WrappedFunction cond;
    private AbstractAction action, ins;
    @Override
    public void initialize() {
        super.initialize();
        if (condition != null) cond = FunctionBuilder.INSTANCE.evaluate(condition, Object.class);
        adder.addLazyTask(() -> {
            action = DialogManager.INSTANCE.getAction(name);
            if (action == null) throwNotFundError(name);
            if (condition != null) {
                ins = DialogManager.INSTANCE.getAction(instead);
                if (ins == null) throwNotFundError(instead);
            }
            return Unit.INSTANCE;
        });
    }
    private void throwNotFundError(String n) {
        if (action == null) QuestAdder.Companion.warn("not found error: the action named \"" + n + "\" doesn't exist.");
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (cond != null) {
            var obj = cond.apply(event);
            if (obj instanceof Boolean bool) {
                if (!bool) {
                    if (ins != null) ins.invoke(player, event);
                    return;
                }
            } else QuestAdder.Companion.warn("runtime error: the condition \"" + cond + "\" is not a boolean.");
        }
        if (action != null) action.invoke(player,event);
    }
}
