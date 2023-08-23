package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

import java.util.Optional;

public class EventIslandQuit extends AbstractEvent<IslandQuitEvent> {
    public EventIslandQuit(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandQuitEvent.class);
    }

    @Override
    protected void invoke(IslandQuitEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
