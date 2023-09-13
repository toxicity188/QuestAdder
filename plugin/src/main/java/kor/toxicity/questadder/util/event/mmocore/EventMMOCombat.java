package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;

public class EventMMOCombat extends AbstractEvent<PlayerCombatEvent> {

    @DataField(aliases = "e")
    public boolean enter = true;

    public EventMMOCombat(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerCombatEvent.class);
    }

    @Override
    public void invoke(PlayerCombatEvent event) {
        if (enter != event.entersCombat()) return;
        apply(event.getPlayer());
    }
}
