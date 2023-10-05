package kor.toxicity.questadder.util.event.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class EventMythicKill extends AbstractEvent<MythicMobDeathEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;
    @DataField(aliases = "i")
    public String id;

    public EventMythicKill(QuestAdder adder, AbstractAction action) {
        super(adder, action, MythicMobDeathEvent.class);
    }

    @Override
    public void invoke(@NotNull MythicMobDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            var killer = livingEntity.getKiller();
            if (killer == null) return;
            if (id != null && !event.getMob().getMobType().equals(id)) return;
            if (type != null && livingEntity.getType() != type) return;
            if (name != null) {
                var n = livingEntity.getCustomName();
                if (n != null && !n.equals(name)) return;
            }
            apply(killer);
        }
    }
}
