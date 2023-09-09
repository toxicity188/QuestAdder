package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActSound extends AbstractAction {
    @DataField(aliases = "n", throwIfNull = true)
    public String name;
    @DataField(aliases = "v")
    public float volume = 1F;
    @DataField(aliases = "p")
    public float pitch = 1F;

    public ActSound(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        player.playSound(player.getLocation(),name,volume,pitch);
    }
}
