package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ActPotion extends AbstractAction {
    @DataField(aliases = "t", throwIfNull = true)
    public PotionEffectType type;

    @DataField(aliases = "d")
    public int duration = 20;
    @DataField(aliases = "a")
    public int amplifier = 0;

    @DataField(aliases = "am")
    public boolean ambient = true;
    @DataField(aliases = "p")
    public boolean particles = false;
    @DataField(aliases = "i")
    public boolean icon = false;

    public ActPotion(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        player.addPotionEffect(new PotionEffect(type,duration,amplifier,ambient,particles,icon));
    }
}
