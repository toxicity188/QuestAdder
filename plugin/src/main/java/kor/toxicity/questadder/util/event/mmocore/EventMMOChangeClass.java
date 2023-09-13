package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;

public class EventMMOChangeClass extends AbstractEvent<PlayerChangeClassEvent> {
    @DataField(aliases = "i")
    public String id;

    public EventMMOChangeClass(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerChangeClassEvent.class);
    }

    @Override
    public void invoke(PlayerChangeClassEvent event) {
        if (id != null && !event.getNewClass().getId().equals(id)) return;
        apply(event.getPlayer());
    }
}
