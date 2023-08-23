package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

import java.util.Optional;

public class EventIslandCreate extends AbstractEvent<IslandCreateEvent> {

    public EventIslandCreate(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandCreateEvent.class);
    }

    @Override
    protected void invoke(IslandCreateEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
