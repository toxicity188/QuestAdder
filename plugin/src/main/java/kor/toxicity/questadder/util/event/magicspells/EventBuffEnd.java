package kor.toxicity.questadder.util.event.magicspells;

import com.nisovin.magicspells.events.BuffEndEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventBuffEnd extends AbstractEvent<BuffEndEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventBuffEnd(QuestAdder adder, AbstractAction action) {
        super(adder, action, BuffEndEvent.class);
    }

    @Override
    public void invoke(@NotNull BuffEndEvent event) {
        if (name != null && !event.getBuffSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player);
    }
}
