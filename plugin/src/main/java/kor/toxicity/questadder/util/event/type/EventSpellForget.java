package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.SpellForgetEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventSpellForget extends AbstractEvent<SpellForgetEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellForget(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellForgetEvent.class);
    }

    @Override
    protected void invoke(SpellForgetEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        apply(event.getForgetter());
    }
}
