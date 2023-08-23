package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.EntityType;

public class EventNPCClick extends AbstractEvent<NPCRightClickEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;
    @DataField(aliases = "i")
    public int id = -1;

    public EventNPCClick(QuestAdder adder, AbstractAction action, Class<NPCRightClickEvent> clazz) {
        super(adder, action, clazz);
    }

    @Override
    protected void invoke(NPCRightClickEvent event) {
        var entity = event.getNPC().getEntity();
        if (type != null && entity.getType() != type) return;
        if (name != null) {
            var n = entity.customName();
            if (n != null && !PlainTextComponentSerializer.plainText().serialize(n).equals(name)) return;
        }
        if (id >= 0 && event.getNPC().getId() != id) return;
        apply(event.getClicker());
    }
}
