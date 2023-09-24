package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class EventQuit extends AbstractEvent<PlayerQuitEvent> {
    public EventQuit(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerQuitEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerQuitEvent event) {
        apply(event.getPlayer());
    }
}
