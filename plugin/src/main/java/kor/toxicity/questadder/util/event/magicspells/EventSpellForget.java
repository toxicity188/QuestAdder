package kor.toxicity.questadder.util.event.magicspells;

import com.nisovin.magicspells.events.SpellForgetEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventSpellForget extends AbstractEvent<SpellForgetEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellForget(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellForgetEvent.class);
    }

    @Override
    public void invoke(SpellForgetEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        apply(event.getForgetter());
    }
}
