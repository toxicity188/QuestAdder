package kor.toxicity.questadder.util.event.mmoitems;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmoitems.api.event.item.UnsocketGemStoneEvent;
import net.Indyuce.mmoitems.api.interaction.GemStone;
import org.jetbrains.annotations.NotNull;

public class EventMMOUnsocketGemStone extends AbstractEvent<UnsocketGemStoneEvent> {
    public EventMMOUnsocketGemStone(QuestAdder adder, AbstractAction action) {
        super(adder, action, UnsocketGemStoneEvent.class);
    }

    @Override
    public void invoke(@NotNull UnsocketGemStoneEvent event) {
        apply(event.getPlayer());
    }
}
