package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.NavigateEndEvent;
import kor.toxicity.questadder.event.NavigateFailEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

public class EventNavigateEnd extends AbstractEvent<NavigateEndEvent> {
    public EventNavigateEnd(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateEndEvent.class);
    }

    @Override
    protected void invoke(NavigateEndEvent event) {
        apply(event.getPlayer(),event);
    }
}
