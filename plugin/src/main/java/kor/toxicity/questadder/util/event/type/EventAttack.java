package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.extension.ComponentsKt;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
    protected void invoke(EntityDamageByEntityEvent event) {
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
