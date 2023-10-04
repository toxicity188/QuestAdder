package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.util.ComponentReader;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ActMessage extends AbstractAction {
    @DataField(aliases = "m",throwIfNull = true)
    public String message;

    private ComponentReader<QuestAdderEvent> reader;

    public ActMessage(QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        reader = new ComponentReader<>(message);
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var component = reader.createComponent(event, Collections.emptyMap());
        if (component != null) {
            player.sendMessage(component);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
}
