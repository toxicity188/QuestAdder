package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.SpellTargetEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;

public class EventSpellTarget extends AbstractEvent<SpellTargetEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellTarget(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellTargetEvent.class);
    }

    @Override
    protected void invoke(SpellTargetEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player,event.getSpellArgs());
    }
}
