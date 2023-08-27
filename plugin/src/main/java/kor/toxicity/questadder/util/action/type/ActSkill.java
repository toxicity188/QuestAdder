package kor.toxicity.questadder.util.action.type;

import io.lumine.mythic.bukkit.MythicBukkit;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActSkill extends AbstractAction {
    @DataField(aliases = "n", throwIfNull = true)
    public String name;

    public ActSkill(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        if (!MythicBukkit.inst().getAPIHelper().castSkill(player, name)) QuestAdder.Companion.warn("The skill named \"" + name + "\" doesn't exist.");
    }
}
