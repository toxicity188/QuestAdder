package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventQuit extends AbstractEvent<PlayerQuitEvent> {
    public EventQuit(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerQuitEvent.class);
    }

    @Override
    protected void invoke(PlayerQuitEvent event) {
        apply(event.getPlayer());
    }
}
