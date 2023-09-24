package kor.toxicity.questadder.util.event.mmocore;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import org.jetbrains.annotations.NotNull;

public class EventMMOLevelUp extends AbstractEvent<PlayerLevelUpEvent> {

    @DataField
    public int min = Integer.MIN_VALUE;
    @DataField
    public int max = Integer.MAX_VALUE;
    public EventMMOLevelUp(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerLevelUpEvent.class);
    }

    @Override
    public void invoke(@NotNull PlayerLevelUpEvent event) {
        var amount = event.getNewLevel() - event.getOldLevel();
        if (amount > max || amount < min) return;
        apply(event.getPlayer());
    }
}
