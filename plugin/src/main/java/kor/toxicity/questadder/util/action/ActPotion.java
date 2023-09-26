package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ActPotion extends AbstractAction {
    @DataField(aliases = "t", throwIfNull = true)
    public String type;

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

    private PotionEffectType t;
    @Override
    public void initialize() {
        super.initialize();
        t = PotionEffectType.getByName(type);
        if (t == null) throw new RuntimeException("the type named \"" + type + "\" doesn't exist.");
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        player.addPotionEffect(new PotionEffect(t,duration,amplifier,ambient,particles,icon));
        return ActionResult.SUCCESS;
    }
}
