package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.manager.NavigationManager;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActEndNavigate extends AbstractAction {
    public ActEndNavigate(@NotNull QuestAdder adder) {
        super(adder);
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        NavigationManager.INSTANCE.endNavigate(player);
        return ActionResult.SUCCESS;
    }
}
