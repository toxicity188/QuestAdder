package kor.toxicity.questadder.util.event.magicspells;

import com.nisovin.magicspells.events.SpellLearnEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

public class EventSpellLearn extends AbstractEvent<SpellLearnEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellLearn(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellLearnEvent.class);
    }

    @Override
    public void invoke(@NotNull SpellLearnEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        apply(event.getLearner());
    }
}
