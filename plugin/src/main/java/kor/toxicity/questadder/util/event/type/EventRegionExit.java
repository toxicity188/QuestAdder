package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.RegionExitEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventRegionExit extends AbstractEvent<RegionExitEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventRegionExit(QuestAdder adder, AbstractAction action) {
        super(adder, action, RegionExitEvent.class);
    }

    @Override
    protected void invoke(RegionExitEvent event) {
        if (name != null && !event.getRegion().getId().equals(name)) return;
        apply(event.getPlayer(),event);
    }
}
