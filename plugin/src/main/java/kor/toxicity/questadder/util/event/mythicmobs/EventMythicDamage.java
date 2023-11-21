package kor.toxicity.questadder.util.event.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventMythicDamage extends AbstractEvent<MythicDamageEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "i")
    public String id;

    public EventMythicDamage(QuestAdder adder, AbstractAction action) {
        super(adder, action, MythicDamageEvent.class);
    }

    @Override
    public void invoke(@NotNull MythicDamageEvent event) {
        var caster = event.getCaster();
        if (type != null && caster.getEntity().getBukkitEntity().getType() != type) return;
        if (id != null && caster instanceof ActiveMob mob && !mob.getMobType().equals(id)) return;
        if (event.getCaster().getEntity().getBukkitEntity() instanceof Player player) apply(player);
    }
}
