package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.extension.PlayersKt;
import kor.toxicity.questadder.manager.ItemManager;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ActTake extends AbstractAction {

    @DataField(aliases = "i",throwIfNull = true)
    public String item;
    private ItemStack stack;
    public ActTake(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        stack = ItemManager.INSTANCE.getItem(item);
        if (stack == null) throw new RuntimeException("the item named \"" + item + "\" doesn't exist.");
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        PlayersKt.take(player,stack);
        return ActionResult.SUCCESS;
    }
}
