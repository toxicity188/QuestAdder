package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.manager.SlateManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActSlate extends AbstractAction {
    @DataField(aliases = "b")
    public boolean back = true;

    public ActSlate(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull ActionInvokeEvent event) {
        SlateManager.INSTANCE.slate(player,back);
    }
}
