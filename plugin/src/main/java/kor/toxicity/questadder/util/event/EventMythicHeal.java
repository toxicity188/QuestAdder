package kor.toxicity.questadder.util.event;

import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EventMythicHeal extends AbstractEvent<MythicHealMechanicEvent> {

    @DataField(aliases = "t")
    public EntityType type;

    public EventMythicHeal(QuestAdder adder, AbstractAction action) {
        super(adder, action, MythicHealMechanicEvent.class);
    }

    @Override
    public void invoke(MythicHealMechanicEvent event) {
        if (type != null && event.getTarget().getType() != type) return;
        if (event.getEntity() instanceof Player player) apply(player);
    }
}
