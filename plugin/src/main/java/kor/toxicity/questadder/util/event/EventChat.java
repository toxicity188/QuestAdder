package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

public class EventChat extends AbstractEvent<AsyncPlayerChatEvent> {
    @DataField(aliases = "m")
    public String message;

    public EventChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, AsyncPlayerChatEvent.class);
    }

    @Override
    public void invoke(@NotNull AsyncPlayerChatEvent event) {
        if (message != null && !event.getMessage().equals(message)) return;
        apply(event.getPlayer());
    }
}
