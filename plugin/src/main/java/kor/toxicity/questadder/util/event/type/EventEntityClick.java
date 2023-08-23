package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class EventEntityClick extends AbstractEvent<PlayerInteractAtEntityEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;

    public EventEntityClick(QuestAdder adder, AbstractAction action, Class<PlayerInteractAtEntityEvent> clazz) {
        super(adder, action, clazz);
    }

    @Override
    protected void invoke(PlayerInteractAtEntityEvent event) {
        var entity = event.getRightClicked();
        if (type != null && entity.getType() != type) return;
        if (name != null) {
            var n = entity.customName();
            if (n != null && !PlainTextComponentSerializer.plainText().serialize(n).equals(name)) return;
        }
        apply(event.getPlayer());
    }
}
