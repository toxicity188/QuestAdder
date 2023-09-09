package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ActClearPotion extends AbstractAction {
    @DataField(aliases = "t", throwIfNull = true)
    public PotionEffectType type;

    public ActClearPotion(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(Player player, QuestAdderEvent event) {
        player.removePotionEffect(type);
    }
}
