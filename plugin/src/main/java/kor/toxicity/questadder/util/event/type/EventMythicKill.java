package kor.toxicity.questadder.util.event.type;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

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
    protected void invoke(MythicMobDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            var killer = livingEntity.getKiller();
            if (killer == null) return;
            if (id != null && event.getMob().getMobType().equals(id)) return;
            if (type != null && livingEntity.getType() != type) return;
            if (name != null) {
                var n = livingEntity.customName();
                if (n != null && !PlainTextComponentSerializer.plainText().serialize(n).equals(name)) return;
            }
            apply(killer);
        }
    }
}
