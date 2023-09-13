package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.unlocking.ItemLockedEvent;

public class EventMMOItemLock extends AbstractEvent<ItemLockedEvent> {
    @DataField(aliases = "i")
    public String id;

    public EventMMOItemLock(QuestAdder adder, AbstractAction action) {
        super(adder, action, ItemLockedEvent.class);
    }

    @Override
    public void invoke(ItemLockedEvent event) {
        if (id != null && !event.getItemId().equals(id)) return;
        apply(event.getPlayer());
    }
}
