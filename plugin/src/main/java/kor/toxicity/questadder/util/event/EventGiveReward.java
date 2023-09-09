package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.GiveRewardEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

public class EventGiveReward extends AbstractEvent<GiveRewardEvent> {
    public EventGiveReward(QuestAdder adder, AbstractAction action) {
        super(adder, action, GiveRewardEvent.class);
    }

    @Override
    public void invoke(GiveRewardEvent event) {
        apply(event.getPlayer(),event);
    }
}
