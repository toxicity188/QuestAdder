package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandOpenEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

import java.util.Optional;

public class EventIslandOpen extends AbstractEvent<IslandOpenEvent> {
    public EventIslandOpen(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandOpenEvent.class);
    }

    @Override
    public void invoke(IslandOpenEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
