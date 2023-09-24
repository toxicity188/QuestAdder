package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.DialogManager;
import kor.toxicity.questadder.mechanic.Dialog;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActDialog extends AbstractAction {

    @DataField(aliases = "d", throwIfNull = true)
    public String dialog;
    @DataField(aliases = "n", throwIfNull = true)
    public String npc;

    private Dialog d;

    public ActDialog(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        adder.addLazyTask(() -> {
            d = DialogManager.INSTANCE.getDialog(dialog);
            if (d == null) QuestAdderBukkit.Companion.warn("the dialog named \"" + dialog + "\" doesn't exist.");
            if (DialogManager.INSTANCE.getQuestNPC(npc) == null) QuestAdderBukkit.Companion.warn("the npc named \"" + npc + "\" doesn't exist.");
        });
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (d != null) {
            var n = DialogManager.INSTANCE.getNPC(npc);
            if (n != null) d.start(player, n);
        }
    }
}
