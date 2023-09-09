package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.RegionEnterEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventRegionEnter extends AbstractEvent<RegionEnterEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventRegionEnter(QuestAdder adder, AbstractAction action) {
        super(adder, action, RegionEnterEvent.class);
    }

    @Override
    public void invoke(RegionEnterEvent event) {
        if (name != null && !event.getRegion().getId().equals(name)) return;
        apply(event.getPlayer(),event);
    }
}
