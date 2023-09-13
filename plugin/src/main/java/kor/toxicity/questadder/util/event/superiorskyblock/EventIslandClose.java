package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandCloseEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

import java.util.Optional;

public class EventIslandClose extends AbstractEvent<IslandCloseEvent> {
    public EventIslandClose(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandCloseEvent.class);
    }

    @Override
    public void invoke(IslandCloseEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
