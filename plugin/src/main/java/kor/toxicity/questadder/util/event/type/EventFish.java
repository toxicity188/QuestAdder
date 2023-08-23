package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;

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
    protected void invoke(PlayerFishEvent event) {
        if (predicate.test(event)) apply(event.getPlayer());
    }
}
