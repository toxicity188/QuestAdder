package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.extension.PlayersKt;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.manager.ItemManager;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.mechanic.sender.ItemDialogSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ActGive extends AbstractAction {

    @DataField(aliases = "i")
    public String item;
    @DataField(aliases = "s")
    public String sender;

    private ItemStack stack;
    public ActGive(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        adder.addLazyTask(() -> {
            if (item != null) stack = ItemManager.INSTANCE.getItem(item);
            if (sender != null && DialogManager.INSTANCE.getDialogSender(sender) instanceof ItemDialogSender s) stack = s.getItem();
            if (stack == null) QuestAdderBukkit.Companion.warn("unable to find item stack.");
        });
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (stack != null) {
            PlayersKt.give(player,stack);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}
