package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.concurrent.LazyRunnable;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.util.builder.FunctionBuilder;
import kor.toxicity.questadder.util.function.WrappedFunction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActAction extends AbstractAction {

    @DataField(aliases = {"if","cond"})
    public String condition;

    @DataField(aliases = "n",throwIfNull = true)
    public String name;

    @DataField(aliases = "i")
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
        adder.addLazyTask(LazyRunnable.emptyOf(() -> {
            action = DialogManager.INSTANCE.getAction(name);
            if (action == null) throwNotFundError(name);
            if (condition != null && instead != null) {
                ins = DialogManager.INSTANCE.getAction(instead);
                if (ins == null) throwNotFundError(instead);
            }
        }));
    }
    private void throwNotFundError(String n) {
        if (action == null) QuestAdderBukkit.Companion.warn("not found error: the action named \"" + n + "\" doesn't exist.");
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (cond != null) {
            var obj = cond.apply(event);
            if (obj instanceof Boolean bool) {
                if (!bool) {
                    if (ins != null) ins.invoke(player, event);
                    return ActionResult.SUCCESS;
                }
            } else {
                QuestAdderBukkit.Companion.warn("runtime error: the condition \"" + cond + "\" is not a boolean.");
                return ActionResult.FAIL;
            }
        }
        if (action != null) action.invoke(player,event);
        return ActionResult.SUCCESS;
    }
}
