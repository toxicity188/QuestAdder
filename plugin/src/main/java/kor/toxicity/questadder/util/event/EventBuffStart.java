package kor.toxicity.questadder.util.event;

import com.nisovin.magicspells.events.BuffStartEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;

public class EventBuffStart extends AbstractEvent<BuffStartEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventBuffStart(QuestAdder adder, AbstractAction action) {
        super(adder, action, BuffStartEvent.class);
    }

    @Override
    public void invoke(BuffStartEvent event) {
        if (name != null && !event.getBuffSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player);
    }
}
