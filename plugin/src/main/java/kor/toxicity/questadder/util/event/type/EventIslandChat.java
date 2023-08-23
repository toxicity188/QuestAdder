package kor.toxicity.questadder.util.event.type;

import com.bgsoftware.superiorskyblock.api.events.IslandChatEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

import java.util.Optional;

public class EventIslandChat extends AbstractEvent<IslandChatEvent> {

    @DataField(aliases = "m")
    public String message;

    public EventIslandChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandChatEvent.class);
    }

    @Override
    protected void invoke(IslandChatEvent event) {
        if (message != null && !event.getMessage().equals(message)) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
