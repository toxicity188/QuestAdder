package kor.toxicity.questadder.util.event;

import com.nisovin.magicspells.events.SpellTargetEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;

public class EventSpellTarget extends AbstractEvent<SpellTargetEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellTarget(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellTargetEvent.class);
    }

    @Override
    public void invoke(SpellTargetEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player,event.getSpellArgs());
    }
}
