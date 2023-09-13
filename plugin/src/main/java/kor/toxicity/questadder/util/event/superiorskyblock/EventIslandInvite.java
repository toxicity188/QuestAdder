package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;

import java.util.Optional;

public class EventIslandInvite extends AbstractEvent<IslandInviteEvent> {


    public EventIslandInvite(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandInviteEvent.class);
    }

    @Override
    public void invoke(IslandInviteEvent event) {
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
