package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.experience.EXPSource;

public class EventMMOExpGain extends AbstractEvent<PlayerExperienceGainEvent> {

    @DataField
    public double min = Double.MIN_VALUE;
    @DataField
    public double max = Double.MAX_VALUE;
    @DataField(aliases = "s")
    public EXPSource source;
    public EventMMOExpGain(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerExperienceGainEvent.class);
    }

    @Override
    public void invoke(PlayerExperienceGainEvent event) {
        if (source != null && event.getSource() != source) return;
        var amount = event.getExperience();
        if (amount > max || amount < min) return;
        apply(event.getPlayer());
    }
}
