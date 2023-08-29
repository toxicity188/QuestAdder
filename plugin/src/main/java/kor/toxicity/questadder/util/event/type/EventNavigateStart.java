package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.NavigateStartEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventNavigateStart extends AbstractEvent<NavigateStartEvent> {

    @DataField(aliases = "k")
    public String key;

    public EventNavigateStart(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateStartEvent.class);
    }

    @Override
    protected void invoke(NavigateStartEvent event) {
        if (key != null && !key.equals(event.getNamedLocation().getKey())) return;
        apply(event.getPlayer(),event);
    }
}
