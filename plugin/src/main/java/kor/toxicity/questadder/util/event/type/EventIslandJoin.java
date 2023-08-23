package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

import java.util.Optional;

public class EventIslandJoin extends AbstractEvent<IslandJoinEvent> {

    @DataField(aliases = "c")
    public IslandJoinEvent.Cause cause;

    public EventIslandJoin(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandJoinEvent.class);
    }

    @Override
    protected void invoke(IslandJoinEvent event) {
        if (cause != null && event.getCause() != cause) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
