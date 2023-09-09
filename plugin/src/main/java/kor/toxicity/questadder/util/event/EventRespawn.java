package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class EventRespawn extends AbstractEvent<PlayerRespawnEvent> {
    public EventRespawn(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerRespawnEvent.class);
    }

    @Override
    public void invoke(PlayerRespawnEvent event) {
        apply(event.getPlayer());
    }
}
