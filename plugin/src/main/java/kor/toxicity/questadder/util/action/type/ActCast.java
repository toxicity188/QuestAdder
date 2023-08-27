package kor.toxicity.questadder.util.action.type;

import com.nisovin.magicspells.MagicSpells;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActCast extends AbstractAction {

    @DataField(aliases = "n", throwIfNull = true)
    public String name;

    public ActCast(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var spell = MagicSpells.getSpellByInGameName(name);
        if (spell != null) spell.cast(player);
        else QuestAdder.Companion.warn("The skill named \"" + name + "\" doesn't exist.");
    }
}
