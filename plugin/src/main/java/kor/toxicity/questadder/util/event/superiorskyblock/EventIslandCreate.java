package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EventIslandCreate extends AbstractEvent<IslandCreateEvent> {

    public EventIslandCreate(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandCreateEvent.class);
    }

    @Override
    public void invoke(@NotNull IslandCreateEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
