
package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.NavigateCompleteEvent;
import kor.toxicity.questadder.event.NavigateStartEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventNavigateComplete extends AbstractEvent<NavigateCompleteEvent> {

    @DataField(aliases = "k")
    public String key;

    public EventNavigateComplete(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateCompleteEvent.class);
    }

    @Override
    protected void invoke(NavigateCompleteEvent event) {
        if (key != null && !key.equals(event.getNamedLocation().getKey())) return;
        apply(event.getPlayer(),event);
    }
}
