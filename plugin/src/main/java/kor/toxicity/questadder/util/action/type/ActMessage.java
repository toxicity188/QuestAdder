package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.util.ComponentReader;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActMessage extends AbstractAction {
    @DataField(aliases = "m",throwIfNull = true)
    public String message;

    private ComponentReader<ActionInvokeEvent> reader;

    public ActMessage(QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        reader = new ComponentReader<>(message);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull ActionInvokeEvent event) {
        var component = reader.createComponent(event);
        if (component != null) player.sendMessage(component);
        else player.sendMessage("error!");
    }
}
