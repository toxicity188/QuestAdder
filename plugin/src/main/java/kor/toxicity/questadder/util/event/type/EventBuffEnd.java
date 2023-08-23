package kor.toxicity.questadder.util.event.type;

import com.nisovin.magicspells.events.BuffEndEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;

public class EventBuffEnd extends AbstractEvent<BuffEndEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventBuffEnd(QuestAdder adder, AbstractAction action) {
        super(adder, action, BuffEndEvent.class);
    }

    @Override
    protected void invoke(BuffEndEvent event) {
        if (name != null && !event.getBuffSpell().getName().equals(name)) return;
        if (event.getCaster() instanceof Player player) apply(player);
    }
}
