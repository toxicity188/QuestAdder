package kor.toxicity.questadder.util.event.mmoitems;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmoitems.api.event.item.ApplyGemStoneEvent;
import net.Indyuce.mmoitems.api.interaction.GemStone;

public class EventMMOApplyGemStone extends AbstractEvent<ApplyGemStoneEvent> {
    @DataField(aliases = "r")
    public GemStone.ResultType result;
    public EventMMOApplyGemStone(QuestAdder adder, AbstractAction action) {
        super(adder, action, ApplyGemStoneEvent.class);
    }

    @Override
    public void invoke(ApplyGemStoneEvent event) {
        if (result != null && event.getResult() != result) return;
        apply(event.getPlayer());
    }
}
