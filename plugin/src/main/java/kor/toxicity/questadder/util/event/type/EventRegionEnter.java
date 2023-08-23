package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.RegionEnterEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventRegionEnter extends AbstractEvent<RegionEnterEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventRegionEnter(QuestAdder adder, AbstractAction action) {
        super(adder, action, RegionEnterEvent.class);
    }

    @Override
    protected void invoke(RegionEnterEvent event) {
        if (name != null && !event.getRegion().getId().equals(name)) return;
        apply(event.getPlayer(),event);
    }
}
