package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.NavigateStartEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

public class EventNavigateStart extends AbstractEvent<NavigateStartEvent> {

    @DataField(aliases = "k")
    public String key;

    public EventNavigateStart(QuestAdder adder, AbstractAction action) {
        super(adder, action, NavigateStartEvent.class);
    }

    @Override
    public void invoke(@NotNull NavigateStartEvent event) {
        if (key != null && !key.equals(event.getNamedLocation().getKey())) return;
        apply(event.getPlayer(),event);
    }
}
