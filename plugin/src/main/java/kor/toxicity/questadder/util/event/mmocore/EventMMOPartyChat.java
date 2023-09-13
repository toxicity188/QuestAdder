package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.social.PartyChatEvent;

public class EventMMOPartyChat extends AbstractEvent<PartyChatEvent> {
    @DataField(aliases = "m")
    public String message;
    public EventMMOPartyChat(QuestAdder adder, AbstractAction action) {
        super(adder, action, PartyChatEvent.class);
    }

    @Override
    public void invoke(PartyChatEvent event) {
        if (message != null && !message.equals(event.getMessage())) return;
        apply(event.getPlayer());
    }
}
