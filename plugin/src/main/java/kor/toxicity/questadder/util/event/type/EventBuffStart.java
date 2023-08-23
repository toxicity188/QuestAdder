package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.BuffStartEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;

public class EventBuffStart extends AbstractEvent<BuffStartEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventBuffStart(QuestAdder adder, AbstractAction action) {
        super(adder, action, BuffStartEvent.class);
    }

    @Override
    protected void invoke(BuffStartEvent event) {
        if (name != null && !event.getBuffSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player);
    }
}
