package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActRemove extends AbstractAction {
    @DataField(aliases = "n",throwIfNull = true)
    public String name;

    public ActRemove(QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var data = QuestAdderBukkit.Companion.getPlayerData(player);
        if (data != null) data.remove(name);
    }
}
