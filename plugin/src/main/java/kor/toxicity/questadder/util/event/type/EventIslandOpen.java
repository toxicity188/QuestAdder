package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandOpenEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

import java.util.Optional;

public class EventIslandOpen extends AbstractEvent<IslandOpenEvent> {
    public EventIslandOpen(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandOpenEvent.class);
    }

    @Override
    protected void invoke(IslandOpenEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
