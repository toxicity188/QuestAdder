package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.SpellApplyDamageEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;

public class EventSpellDamage extends AbstractEvent<SpellApplyDamageEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellDamage(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellApplyDamageEvent.class);
    }

    @Override
    protected void invoke(SpellApplyDamageEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player);
    }
}
