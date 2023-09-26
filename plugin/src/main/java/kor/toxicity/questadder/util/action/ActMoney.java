package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.extension.PlayersKt;
import kor.toxicity.questadder.util.builder.FunctionBuilder;
import kor.toxicity.questadder.util.function.WrappedFunction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActMoney extends AbstractAction {

    @DataField(aliases = "a")
    public Action action = Action.GIVE;
    @DataField(aliases = "am", throwIfNull = true)
    public String amount;

    private WrappedFunction function;

    public ActMoney(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        function = FunctionBuilder.INSTANCE.evaluate(amount, Object.class);
        if (!Number.class.isAssignableFrom(function.getReturnType())) throw new RuntimeException("this format is not a number: " + amount);
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var find = function.apply(event);
        if (find instanceof Number number) {
            action.executeMoney(player, number.doubleValue());
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    public enum Action {
        GIVE {
            @Override
            public void executeMoney(Player player, double money) {
                if (money > 0) {
                    PlayersKt.addMoney(player,money);
                } else if (money < 0) {
                    PlayersKt.removeMoney(player,-money);
                }
            }
        },
        SET {
            @Override
            public void executeMoney(Player player, double money) {
                if (money > 0) {
                    var amount = PlayersKt.getMoney(player) - money;
                    if (amount > 0) PlayersKt.removeMoney(player, money);
                    else if (amount < 0) PlayersKt.addMoney(player, -money);
                }
            }
        }
        ;
        public abstract void executeMoney(Player player, double money);
    }
}
