package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;

public class EventResourcePack extends AbstractEvent<PlayerResourcePackStatusEvent> {

    @DataField(aliases = "s")
    public PlayerResourcePackStatusEvent.Status status;

    public EventResourcePack(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerResourcePackStatusEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerResourcePackStatusEvent event) {
        if (status != null && status != event.getStatus()) return;
        apply(event.getPlayer());
    }
}
