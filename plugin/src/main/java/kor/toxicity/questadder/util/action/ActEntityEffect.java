package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActEntityEffect extends AbstractAction {
    @DataField(aliases = "e", throwIfNull = true)
    public EntityEffect effect;

    public ActEntityEffect(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!effect.getApplicable().isAssignableFrom(Player.class)) throw new RuntimeException("inapplicable effect: " + effect.name().toLowerCase());
    }

    @Override
    public @NotNull ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        player.playEffect(effect);
        return ActionResult.SUCCESS;
    }
}
