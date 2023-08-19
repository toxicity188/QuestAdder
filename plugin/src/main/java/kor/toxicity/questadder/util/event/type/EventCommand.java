package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EventCommand extends AbstractEvent<PlayerCommandPreprocessEvent> {

    @DataField(aliases = "c")
    public String command;

    public EventCommand(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerCommandPreprocessEvent.class);
    }

    @Override
    protected void invoke(PlayerCommandPreprocessEvent event) {
        if (command != null && !event.getMessage().substring(1).startsWith(command)) return;
        apply(event.getPlayer());
    }
}
