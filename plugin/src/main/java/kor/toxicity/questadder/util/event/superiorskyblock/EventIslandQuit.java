package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

import java.util.Optional;

public class EventIslandQuit extends AbstractEvent<IslandQuitEvent> {
    public EventIslandQuit(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandQuitEvent.class);
    }

    @Override
    public void invoke(IslandQuitEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
