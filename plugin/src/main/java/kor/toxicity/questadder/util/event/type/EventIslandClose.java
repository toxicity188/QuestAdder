package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandCloseEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

import java.util.Optional;

public class EventIslandClose extends AbstractEvent<IslandCloseEvent> {
    public EventIslandClose(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandCloseEvent.class);
    }

    @Override
    protected void invoke(IslandCloseEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
