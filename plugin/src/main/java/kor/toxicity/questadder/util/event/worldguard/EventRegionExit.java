package kor.toxicity.questadder.util.event.worldguard;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.RegionExitEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

public class EventRegionExit extends AbstractEvent<RegionExitEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventRegionExit(QuestAdder adder, AbstractAction action) {
        super(adder, action, RegionExitEvent.class);
    }

    @Override
    public void invoke(@NotNull RegionExitEvent event) {
        if (name != null && !event.getRegion().getId().equals(name)) return;
        apply(event.getPlayer(),event);
    }
}
