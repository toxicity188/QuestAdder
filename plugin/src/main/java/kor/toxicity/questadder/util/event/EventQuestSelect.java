package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestSelectEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventQuestSelect extends AbstractEvent<QuestSelectEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestSelect(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestSelectEvent.class);
    }

    @Override
    public void invoke(QuestSelectEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
