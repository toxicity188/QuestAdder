package kor.toxicity.questadder.util.event.magicspells;

import com.nisovin.magicspells.events.SpellCastedEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventSpellCast extends AbstractEvent<SpellCastedEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventSpellCast(QuestAdder adder, AbstractAction action) {
        super(adder, action, SpellCastedEvent.class);
    }

    @Override
    public void invoke(@NotNull SpellCastedEvent event) {
        if (name != null && !event.getSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player,event.getSpellArgs());
    }
}
