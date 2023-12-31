package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.UserDataLoadEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.jetbrains.annotations.NotNull;

public class EventJoin extends AbstractEvent<UserDataLoadEvent> {
    public EventJoin(QuestAdder adder, AbstractAction action) {
        super(adder, action, UserDataLoadEvent.class);
    }

    @Override
    public void invoke(@NotNull UserDataLoadEvent event) {
        apply(event.getPlayer());
    }
}
