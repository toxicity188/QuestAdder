package kor.toxicity.questadder.util.event.magicspells;

import com.nisovin.magicspells.events.SpellApplyDamageEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventSpellDamage extends AbstractEvent<SpellApplyDamageEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellDamage(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellApplyDamageEvent.class);
    }

    @Override
    public void invoke(@NotNull SpellApplyDamageEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player);
    }
}
