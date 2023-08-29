package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.GiveRewardEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

public class EventGiveReward extends AbstractEvent<GiveRewardEvent> {
    public EventGiveReward(QuestAdder adder, AbstractAction action) {
        super(adder, action, GiveRewardEvent.class);
    }

    @Override
    protected void invoke(GiveRewardEvent event) {
        apply(event.getPlayer(),event);
    }
}
