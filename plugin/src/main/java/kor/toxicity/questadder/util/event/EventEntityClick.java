package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

public class EventEntityClick extends AbstractEvent<PlayerInteractAtEntityEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;

    public EventEntityClick(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerInteractAtEntityEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerInteractAtEntityEvent event) {
        var entity = event.getRightClicked();
        if (type != null && entity.getType() != type) return;
        if (name != null) {
            var n = entity.getCustomName();
            if (n != null && !n.equals(name)) return;
        }
        apply(event.getPlayer());
    }
}
