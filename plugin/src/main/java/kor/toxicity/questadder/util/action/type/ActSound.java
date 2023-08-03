package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.ActionInvokeEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
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
    public void invoke(@NotNull Player player, @NotNull ActionInvokeEvent event) {
        player.playSound(player.getLocation(),name,volume,pitch);
    }
}
