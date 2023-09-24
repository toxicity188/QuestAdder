package kor.toxicity.questadder.util.event.mythiclib;

import io.lumine.mythic.lib.api.event.mitigation.PlayerDodgeEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMODodge extends AbstractEvent<PlayerDodgeEvent> {
    public EventMMODodge(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerDodgeEvent.class);
    }
    @Override
    public void invoke(@NotNull PlayerDodgeEvent event) {
        apply(event.getPlayer());
    }
}
