package kor.toxicity.questadder.util.event.mmoitems;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import net.Indyuce.mmoitems.api.event.item.ApplySoulboundEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOApplySoulbound extends AbstractEvent<ApplySoulboundEvent> {
    public EventMMOApplySoulbound(QuestAdder adder, AbstractAction action) {
        super(adder, action, ApplySoulboundEvent.class);
    }

    @Override
    public void invoke(@NotNull ApplySoulboundEvent event) {
        apply(event.getPlayer());
    }
}
