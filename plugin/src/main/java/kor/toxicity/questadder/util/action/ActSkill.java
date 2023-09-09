package kor.toxicity.questadder.util.action;

import io.lumine.mythic.bukkit.MythicBukkit;
import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
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
        if (!MythicBukkit.inst().getAPIHelper().castSkill(player, name)) QuestAdderBukkit.Companion.warn("The skill named \"" + name + "\" doesn't exist.");
    }
}
