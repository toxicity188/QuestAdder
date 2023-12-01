package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.concurrent.LazyRunnable;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.mechanic.dialog.Dialog;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActDialog extends AbstractAction {

    @DataField(aliases = "d", throwIfNull = true)
    public String dialog;
    @DataField(aliases = "s", throwIfNull = true)
    public String sender;

    private Dialog d;

    public ActDialog(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        adder.addLazyTask(LazyRunnable.emptyOf(() -> {
            d = DialogManager.INSTANCE.getDialog(dialog);
            if (d == null) QuestAdderBukkit.Companion.warn("the dialog named \"" + dialog + "\" doesn't exist.");
        }));
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (d != null) {
            var n = DialogManager.INSTANCE.getDialogSender(sender);
            if (n != null) {
                d.start(player, n);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }
}
