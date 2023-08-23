package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

import java.util.Optional;

public class EventIslandEnter extends AbstractEvent<IslandEnterEvent> {

    @DataField(aliases = "c")
    public IslandEnterEvent.EnterCause cause;

    public EventIslandEnter(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandEnterEvent.class);
    }

    @Override
    protected void invoke(IslandEnterEvent event) {
        if (cause != null && event.getCause() != cause) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
