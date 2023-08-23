package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class EventRespawn extends AbstractEvent<PlayerRespawnEvent> {
    public EventRespawn(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerRespawnEvent.class);
    }

    @Override
    protected void invoke(PlayerRespawnEvent event) {
        apply(event.getPlayer());
    }
}
