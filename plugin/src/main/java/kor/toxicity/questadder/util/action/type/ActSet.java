package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
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

    @Override
    public void invoke(@NotNull Player player, @NotNull ActionInvokeEvent event) {
        var apply = function.apply(event);
        if (apply != null) {
            var data = QuestAdder.Companion.getPlayerData(player);
            if (data != null) data.set(name,apply);
        }
    }
}
