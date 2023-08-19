package kor.toxicity.questadder.util.event.type;

import io.papermc.paper.event.player.AsyncChatEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.extension.ComponentsKt;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventChat extends AbstractEvent<AsyncChatEvent> {
    @DataField(aliases = "m")
    public String message;

    public EventChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, AsyncChatEvent.class);
    }

    @Override
    protected void invoke(AsyncChatEvent event) {
        if (message != null && !ComponentsKt.onlyText(event.originalMessage()).equals(message)) return;
        apply(event.getPlayer());
    }
}
