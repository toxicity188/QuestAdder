package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.PlayerAttributeUseEvent;

public class EventMMOAttributeUse extends AbstractEvent<PlayerAttributeUseEvent> {
    @DataField(aliases = "i")
    public String id;

    public EventMMOAttributeUse(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerAttributeUseEvent.class);
    }

    @Override
    public void invoke(PlayerAttributeUseEvent event) {
        if (id != null && !event.getAttribute().getId().equals(id)) return;
        apply(event.getPlayer());
    }
}
