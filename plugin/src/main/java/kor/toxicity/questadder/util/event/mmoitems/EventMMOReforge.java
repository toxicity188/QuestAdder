package kor.toxicity.questadder.util.event.mmoitems;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOReforge extends AbstractEvent<MMOItemReforgeFinishEvent> {
    @DataField(aliases = "i")
    public String id;
    public EventMMOReforge(QuestAdder adder, AbstractAction action) {
        super(adder, action, MMOItemReforgeFinishEvent.class);
    }

    @Override
    public void invoke(@NotNull MMOItemReforgeFinishEvent event) {
        if (id != null && !id.equals(event.getID())) return;
        var player = event.getPlayer();
        if (player != null) apply(player);
    }
}
