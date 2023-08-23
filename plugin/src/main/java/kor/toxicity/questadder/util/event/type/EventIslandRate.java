package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandRateEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

import java.util.Optional;

public class EventIslandRate extends AbstractEvent<IslandRateEvent> {

    @DataField
    public int min = Integer.MIN_VALUE;
    @DataField
    public int max = Integer.MIN_VALUE;

    public EventIslandRate(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandRateEvent.class);
    }

    @Override
    protected void invoke(IslandRateEvent event) {
        var ordinal = event.getRating().getValue();
        if (ordinal < min) return;
        if (ordinal > max) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
