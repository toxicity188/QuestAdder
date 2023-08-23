package kor.toxicity.questadder.util.event.type;

import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EventMythicDamage extends AbstractEvent<MythicDamageEvent> {

    @DataField(aliases = "t")
    public EntityType type;

    public EventMythicDamage(QuestAdder adder, AbstractAction action) {
        super(adder, action, MythicDamageEvent.class);
    }

    @Override
    protected void invoke(MythicDamageEvent event) {
        if (type != null && event.getDamageMetadata().getDamager().getEntity().getBukkitEntity().getType() != type) return;
        if (event.getCaster().getEntity().getBukkitEntity() instanceof Player player) apply(player);
    }
}
