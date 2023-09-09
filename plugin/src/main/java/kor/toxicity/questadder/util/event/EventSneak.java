package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class EventSneak extends AbstractEvent<PlayerToggleSneakEvent> {

    @DataField(aliases = "s")
    public boolean sneak = true;

    public EventSneak(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerToggleSneakEvent.class);
    }

    @Override
    public void invoke(PlayerToggleSneakEvent event) {
        var player = event.getPlayer();
        if (sneak != player.isSneaking()) return;
        apply(player);
    }
}
