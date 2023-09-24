package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class EventFish extends AbstractEvent<PlayerFishEvent> {

    @DataField(aliases = "s")
    public PlayerFishEvent.State state;
    @DataField
    public boolean success = true;

    private Predicate<PlayerFishEvent> predicate;

    public EventFish(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerFishEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        predicate = e -> success == e.getCaught() instanceof Item;
        if (state != null) predicate = predicate.and(e -> e.getState() == state);
    }

    @Override
    public void invoke(@NotNull PlayerFishEvent event) {
        if (predicate.test(event)) apply(event.getPlayer());
    }
}
