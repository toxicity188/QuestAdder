package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.extension.ComponentsKt;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

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
    public void invoke(@NotNull EntityDeathEvent event) {
        var entity = event.getEntity();
        var killer = entity.getKiller();
        if (killer != null) {
            if (type != null && event.getEntityType() != type) return;
            if (name != null) {
                var component = entity.getCustomName();
                if (component != null && !component.equals(name)) return;
            }
            apply(killer);
        }
    }
}
