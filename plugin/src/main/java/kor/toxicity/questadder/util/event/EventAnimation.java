package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.jetbrains.annotations.NotNull;

public class EventAnimation extends AbstractEvent<PlayerAnimationEvent> {

    @DataField(aliases = "t")
    public PlayerAnimationType type;

    public EventAnimation(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerAnimationEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerAnimationEvent event) {
        if (type != null && event.getAnimationType() != type) return;
        apply(event.getPlayer());
    }
}
