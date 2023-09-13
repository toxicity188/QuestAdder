package kor.toxicity.questadder.util.event.mmoitems;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import net.Indyuce.mmoitems.api.event.item.BreakSoulboundEvent;

public class EventMMOBreakSoulbound extends AbstractEvent<BreakSoulboundEvent> {
    public EventMMOBreakSoulbound(QuestAdder adder, AbstractAction action) {
        super(adder, action, BreakSoulboundEvent.class);
    }

    @Override
    public void invoke(BreakSoulboundEvent event) {
        apply(event.getPlayer());
    }
}
