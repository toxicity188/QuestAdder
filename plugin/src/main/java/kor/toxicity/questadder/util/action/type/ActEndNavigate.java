package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.NavigationManager;
import kor.toxicity.questadder.util.action.AbstractAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActEndNavigate extends AbstractAction {
    public ActEndNavigate(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(Player player, QuestAdderEvent event) {
        NavigationManager.INSTANCE.endNavigate(player);
    }
}
