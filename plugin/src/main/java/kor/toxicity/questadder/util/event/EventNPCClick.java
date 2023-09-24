package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EventNPCClick extends AbstractEvent<NPCRightClickEvent> {

    @DataField(aliases = "t")
    public EntityType type;
    @DataField(aliases = "n")
    public String name;
    @DataField(aliases = "i")
    public int id = -1;

    public EventNPCClick(QuestAdder adder, AbstractAction action) {
        super(adder, action, NPCRightClickEvent.class);
    }

    @Override
    public void invoke(@NotNull NPCRightClickEvent event) {
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
