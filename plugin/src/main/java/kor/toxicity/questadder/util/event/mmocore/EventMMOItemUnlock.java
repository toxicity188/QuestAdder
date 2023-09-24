package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.unlocking.ItemUnlockedEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOItemUnlock extends AbstractEvent<ItemUnlockedEvent> {
    @DataField(aliases = "i")
    public String id;

    public EventMMOItemUnlock(QuestAdder adder, AbstractAction action) {
        super(adder, action, ItemUnlockedEvent.class);
    }

    @Override
    public void invoke(@NotNull ItemUnlockedEvent event) {
        if (id != null && !event.getItemId().equals(id)) return;
        apply(event.getPlayer());
    }
}
