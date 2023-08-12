package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

public class EventKill extends AbstractEvent<EntityDeathEvent> {
    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;

    public EventKill(QuestAdder adder, AbstractAction action) {
        super(adder, action, EntityDeathEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (name != null) name = name.replace('&','§');
    }

    @Override
    protected void invoke(EntityDeathEvent event) {
        var entity = event.getEntity();
        var killer = entity.getKiller();
        if (killer != null) {
            if (type != null && event.getEntityType() != type) return;
            if (name != null) {
                var component = entity.customName();
                if (component != null && !LegacyComponentSerializer.legacySection().serialize(component).equals(name)) return;
            }
            apply(killer);
        }
    }
}
