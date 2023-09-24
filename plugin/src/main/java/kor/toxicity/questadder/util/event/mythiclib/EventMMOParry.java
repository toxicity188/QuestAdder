package kor.toxicity.questadder.util.event.mythiclib;

import io.lumine.mythic.lib.api.event.mitigation.PlayerParryEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOParry extends AbstractEvent<PlayerParryEvent> {
    public EventMMOParry(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerParryEvent.class);
    }
    @Override
    public void invoke(@NotNull PlayerParryEvent event) {
        apply(event.getPlayer());
    }
}
