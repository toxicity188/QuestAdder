package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.util.builder.FunctionBuilder;
import kor.toxicity.questadder.util.function.WrappedFunction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActSet extends AbstractAction {
    @DataField(aliases = "n",throwIfNull = true)
    public String name;
    @DataField(aliases = "v",throwIfNull = true)
    public String value;

    private WrappedFunction function;

    public ActSet(QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        function = FunctionBuilder.INSTANCE.evaluate(value, Object.class);
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var apply = function.apply(event);
        if (apply != null) {
            var data = QuestAdderBukkit.Companion.getPlayerData(player);
            if (data != null) {
                data.set(name,apply);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }
}
