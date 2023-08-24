package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.SpellCastedEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;

public class EventSpellCast extends AbstractEvent<SpellCastedEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellCast(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellCastedEvent.class);
    }

    @Override
    protected void invoke(SpellCastedEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player,event.getSpellArgs());
    }
}
