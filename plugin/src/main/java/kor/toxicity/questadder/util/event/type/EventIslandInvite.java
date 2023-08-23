package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;

import java.util.Optional;

public class EventIslandInvite extends AbstractEvent<IslandInviteEvent> {


    public EventIslandInvite(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandInviteEvent.class);
    }

    @Override
    protected void invoke(IslandInviteEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
