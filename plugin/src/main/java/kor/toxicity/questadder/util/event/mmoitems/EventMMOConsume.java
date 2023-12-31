package kor.toxicity.questadder.util.event.mmoitems;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import net.Indyuce.mmoitems.api.event.item.ConsumableConsumedEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOConsume extends AbstractEvent<ConsumableConsumedEvent> {
    public EventMMOConsume(QuestAdder adder, AbstractAction action) {
        super(adder, action, ConsumableConsumedEvent.class);
    }

    @Override
    public void invoke(@NotNull ConsumableConsumedEvent event) {
        apply(event.getPlayer());
    }
}
