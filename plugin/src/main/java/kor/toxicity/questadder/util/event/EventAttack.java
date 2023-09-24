package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.extension.ComponentsKt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class EventAttack extends AbstractEvent<EntityDamageByEntityEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;

    public EventAttack(QuestAdder adder, AbstractAction action) {
        super(adder, action, EntityDamageByEntityEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (name != null) name = name.replace("&","§");
    }

    @Override
    public void invoke(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            var victim = event.getEntity();
            if (type != null && victim.getType() != type) return;
            if (name != null) {
                var n = victim.customName();
                if (n != null && !ComponentsKt.onlyText(n).equals(name)) return;
            }
            apply(player);
        }
    }
}
