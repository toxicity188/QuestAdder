package kor.toxicity.questadder.util.event;

import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

import java.util.Optional;

public class EventIslandEnter extends AbstractEvent<IslandEnterEvent> {

    @DataField(aliases = "c")
    public IslandEnterEvent.EnterCause cause;

    public EventIslandEnter(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandEnterEvent.class);
    }

    @Override
    public void invoke(IslandEnterEvent event) {
        if (cause != null && event.getCause() != cause) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
