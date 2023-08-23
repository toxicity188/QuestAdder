package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;

public class EventAnimation extends AbstractEvent<PlayerAnimationEvent> {

    @DataField(aliases = "t")
    public PlayerAnimationType type;

    public EventAnimation(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerAnimationEvent.class);
    }

    @Override
    protected void invoke(PlayerAnimationEvent event) {
        if (type != null && event.getAnimationType() != type) return;
        apply(event.getPlayer());
    }
}
