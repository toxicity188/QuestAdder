package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.UserDataLoadEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

public class EventJoin extends AbstractEvent<UserDataLoadEvent> {
    public EventJoin(QuestAdder adder, AbstractAction action) {
        super(adder, action, UserDataLoadEvent.class);
    }

    @Override
    protected void invoke(UserDataLoadEvent event) {
        apply(event.getPlayer());
    }
}
