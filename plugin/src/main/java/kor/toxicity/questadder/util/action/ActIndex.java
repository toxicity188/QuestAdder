package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.concurrent.LazyRunnable;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.mechanic.npc.QuestNPC;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.util.builder.FunctionBuilder;
import kor.toxicity.questadder.util.function.WrappedFunction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActIndex extends AbstractAction {

    @DataField(aliases = "n", throwIfNull = true)
    public String name;
    @DataField(aliases = "o")
    public Operation operation = Operation.ADD;
    @DataField(aliases = "a", throwIfNull = true)
    public String amount;

    private WrappedFunction function;
    private QuestNPC npc;

    public ActIndex(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        function = FunctionBuilder.INSTANCE.evaluate(amount, Object.class);
        if (!Number.class.isAssignableFrom(function.getReturnType())) throw new RuntimeException("The function \"" + amount + "\" is not a number.");
        adder.addLazyTask(LazyRunnable.emptyOf(() -> {
            npc = DialogManager.INSTANCE.getQuestNPC(name);
            if (npc == null) QuestAdderBukkit.Companion.warn("The NPC named \"" + name + "\" doesn't exist.");
        }));
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (npc != null) {
            var obj = function.apply(event);
            if (obj instanceof Number number) operation.apply(player,npc,number.intValue());
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }


    public enum Operation {
        SET {
            @Override
            void apply(Player player, QuestNPC npc, int amount) {
                npc.setIndex(player,amount);
            }
        },
        ADD {
            @Override
            void apply(Player player, QuestNPC npc, int amount) {
                var i = npc.getIndex(player);
                if (i == null) i = 0;
                npc.setIndex(player, amount + i);
            }
        },
        SUBTRACT {
            @Override
            void apply(Player player, QuestNPC npc, int amount) {
                var i = npc.getIndex(player);
                if (i == null) i = 0;
                npc.setIndex(player, amount - i);
            }
        },
        MULTIPLY {
            @Override
            void apply(Player player, QuestNPC npc, int amount) {
                var i = npc.getIndex(player);
                if (i == null) i = 0;
                npc.setIndex(player, amount * i);
            }
        },
        DIVIDE {
            @Override
            void apply(Player player, QuestNPC npc, int amount) {
                var i = npc.getIndex(player);
                if (i == null) i = 0;
                npc.setIndex(player, amount / i);
            }
        }
        ;
        abstract void apply(Player player, QuestNPC npc, int amount);
    }
}
