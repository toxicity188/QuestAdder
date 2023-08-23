package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class EventSprint extends AbstractEvent<PlayerToggleSprintEvent> {

    @DataField(aliases = "s")
    public boolean sprint = true;

    public EventSprint(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerToggleSprintEvent.class);
    }

    @Override
    protected void invoke(PlayerToggleSprintEvent event) {
        var player = event.getPlayer();
        if (sprint != player.isSprinting()) return;
        apply(player);
    }
}
