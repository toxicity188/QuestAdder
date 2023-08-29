package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.NavigateFailEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

public class EventNavigateFail extends AbstractEvent<NavigateFailEvent> {
    public EventNavigateFail(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateFailEvent.class);
    }

    @Override
    protected void invoke(NavigateFailEvent event) {
        apply(event.getPlayer(),event);
    }
}
