package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class EventResourcePack extends AbstractEvent<PlayerResourcePackStatusEvent> {

    @DataField(aliases = "s")
    public PlayerResourcePackStatusEvent.Status status;

    public EventResourcePack(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerResourcePackStatusEvent.class);
    }

    @Override
    protected void invoke(PlayerResourcePackStatusEvent event) {
        if (status != null && status != event.getStatus()) return;
        apply(event.getPlayer());
    }
}
