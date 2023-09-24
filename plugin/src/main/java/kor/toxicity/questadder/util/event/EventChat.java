package kor.toxicity.questadder.util.event;

import io.papermc.paper.event.player.AsyncChatEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.extension.ComponentsKt;
import org.jetbrains.annotations.NotNull;

public class EventChat extends AbstractEvent<AsyncChatEvent> {
    @DataField(aliases = "m")
    public String message;

    public EventChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, AsyncChatEvent.class);
    }

    @Override
    public void invoke(@NotNull AsyncChatEvent event) {
        if (message != null && !ComponentsKt.onlyText(event.originalMessage()).equals(message)) return;
        apply(event.getPlayer());
    }
}
