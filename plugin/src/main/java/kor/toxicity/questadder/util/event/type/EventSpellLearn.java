package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.SpellLearnEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventSpellLearn extends AbstractEvent<SpellLearnEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellLearn(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellLearnEvent.class);
    }

    @Override
    protected void invoke(SpellLearnEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        apply(event.getLearner());
    }
}
