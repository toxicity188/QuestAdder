package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;

public class EventSprint extends AbstractEvent<PlayerToggleSprintEvent> {

    @DataField(aliases = "s")
    public boolean sprint = true;

    public EventSprint(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerToggleSprintEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerToggleSprintEvent event) {
        var player = event.getPlayer();
        if (sprint != player.isSprinting()) return;
        apply(player);
    }
}
