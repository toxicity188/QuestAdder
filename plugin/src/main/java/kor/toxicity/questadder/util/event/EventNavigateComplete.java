
package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.NavigateCompleteEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventNavigateComplete extends AbstractEvent<NavigateCompleteEvent> {

    @DataField(aliases = "k")
    public String key;

    public EventNavigateComplete(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateCompleteEvent.class);
    }

    @Override
    public void invoke(NavigateCompleteEvent event) {
        if (key != null && !key.equals(event.getNamedLocation().getKey())) return;
        apply(event.getPlayer(),event);
    }
}
