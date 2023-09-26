package kor.toxicity.questadder.util.action;

import com.nisovin.magicspells.MagicSpells;
import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActCast extends AbstractAction {

    @DataField(aliases = "n", throwIfNull = true)
    public String name;

    public ActCast(@NotNull QuestAdder adder) {
        super(adder);
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var spell = MagicSpells.getSpellByInGameName(name);
        if (spell != null) spell.cast(player);
        else {
            QuestAdderBukkit.Companion.warn("The skill named \"" + name + "\" doesn't exist.");
            return ActionResult.FAIL;
        }
        return ActionResult.SUCCESS;
    }
}
