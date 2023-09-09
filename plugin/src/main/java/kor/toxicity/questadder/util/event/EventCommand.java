package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EventCommand extends AbstractEvent<PlayerCommandPreprocessEvent> {

    @DataField(aliases = "c")
    public String command;

    public EventCommand(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerCommandPreprocessEvent.class);
    }

    @Override
    public void invoke(PlayerCommandPreprocessEvent event) {
        if (command != null && !event.getMessage().substring(1).startsWith(command)) return;
        apply(event.getPlayer());
    }
}
