package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class EventSneak extends AbstractEvent<PlayerToggleSneakEvent> {

    @DataField(aliases = "s")
    public boolean sneak = true;

    public EventSneak(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerToggleSneakEvent.class);
    }

    @Override
    protected void invoke(PlayerToggleSneakEvent event) {
        var player = event.getPlayer();
        if (sneak != player.isSneaking()) return;
        apply(player);
    }
}
