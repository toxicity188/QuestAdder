package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.manager.EntityManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActRemoveEntity extends AbstractAction {
    @DataField(aliases = "k", throwIfNull = true)
    public String key;

    public ActRemoveEntity(@NotNull QuestAdder adder) {
        super(adder);
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        EntityManager.INSTANCE.unregister(player, key);
        return ActionResult.SUCCESS;
    }
}
