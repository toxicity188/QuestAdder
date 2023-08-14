package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestSelectEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventQuestSelect extends AbstractEvent<QuestSelectEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestSelect(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestSelectEvent.class);
    }

    @Override
    protected void invoke(QuestSelectEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
