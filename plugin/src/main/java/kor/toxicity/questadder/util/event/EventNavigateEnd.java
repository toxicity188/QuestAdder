package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.NavigateEndEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

public class EventNavigateEnd extends AbstractEvent<NavigateEndEvent> {
    public EventNavigateEnd(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateEndEvent.class);
    }

    @Override
    public void invoke(NavigateEndEvent event) {
        apply(event.getPlayer(),event);
    }
}
