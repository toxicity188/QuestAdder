package kor.toxicity.questadder.util.event.mythiclib;

import io.lumine.mythic.lib.api.event.mitigation.PlayerBlockEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

public class EventMMOBlock extends AbstractEvent<PlayerBlockEvent> {

    @DataField
    public double min = Double.MIN_VALUE;
    @DataField
    public double max = Double.MAX_VALUE;

    public EventMMOBlock(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerBlockEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerBlockEvent event) {
        var damage = event.getDamageBlocked();
        if (damage < min || damage > max) return;
        apply(event.getPlayer());
    }
}
