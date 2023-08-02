package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActRemove extends AbstractAction {
    @DataField(aliases = "n",throwIfNull = true)
    public String name;

    public ActRemove(QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull ActionInvokeEvent event) {
        var data = QuestAdder.Companion.getPlayerData(player);
        if (data != null) data.remove(name);
    }
}
