package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.NavigateFailEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

public class EventNavigateFail extends AbstractEvent<NavigateFailEvent> {
    public EventNavigateFail(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateFailEvent.class);
    }

    @Override
    public void invoke(NavigateFailEvent event) {
        apply(event.getPlayer(),event);
    }
}
