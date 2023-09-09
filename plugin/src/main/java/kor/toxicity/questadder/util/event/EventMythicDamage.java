package kor.toxicity.questadder.util.event;

import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EventMythicDamage extends AbstractEvent<MythicDamageEvent> {

    @DataField(aliases = "t")
    public EntityType type;

    public EventMythicDamage(QuestAdder adder, AbstractAction action) {
        super(adder, action, MythicDamageEvent.class);
    }

    @Override
    public void invoke(MythicDamageEvent event) {
        if (type != null && event.getDamageMetadata().getDamager().getEntity().getBukkitEntity().getType() != type) return;
        if (event.getCaster().getEntity().getBukkitEntity() instanceof Player player) apply(player);
    }
}
