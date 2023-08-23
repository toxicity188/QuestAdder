package kor.toxicity.questadder.util.event.type;

import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EventMythicHeal extends AbstractEvent<MythicHealMechanicEvent> {

    @DataField(aliases = "t")
    public EntityType type;

    public EventMythicHeal(QuestAdder adder, AbstractAction action) {
        super(adder, action, MythicHealMechanicEvent.class);
    }

    @Override
    protected void invoke(MythicHealMechanicEvent event) {
        if (type != null && event.getTarget().getType() != type) return;
        if (event.getEntity() instanceof Player player) apply(player);
    }
}
