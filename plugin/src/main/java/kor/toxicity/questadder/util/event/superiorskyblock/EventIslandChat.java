package kor.toxicity.questadder.util.event.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.events.IslandChatEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EventIslandChat extends AbstractEvent<IslandChatEvent> {

    @DataField(aliases = "m")
    public String message;

    public EventIslandChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, IslandChatEvent.class);
    }

    @Override
    public void invoke(@NotNull IslandChatEvent event) {
        if (message != null && !event.getMessage().equals(message)) return;
        Optional.ofNullable(event.getPlayer()).map(SuperiorPlayer::asPlayer).ifPresent(this::apply);
    }
}
