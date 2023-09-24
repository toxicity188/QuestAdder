package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.social.GuildChatEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOGuildChat extends AbstractEvent<GuildChatEvent> {
    @DataField(aliases = "m")
    public String message;
    public EventMMOGuildChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, GuildChatEvent.class);
    }

    @Override
    public void invoke(@NotNull GuildChatEvent event) {
        if (message != null && !message.equals(event.getMessage())) return;
        apply(event.getPlayer());
    }
}
