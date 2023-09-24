package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EventIslandJoin extends AbstractEvent<IslandJoinEvent> {

    @DataField(aliases = "c")
    public IslandJoinEvent.Cause cause;

    public EventIslandJoin(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandJoinEvent.class);
    }

    @Override
    public void invoke(@NotNull IslandJoinEvent event) {
        if (cause != null && event.getCause() != cause) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
