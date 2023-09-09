package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestCompleteEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventQuestComplete extends AbstractEvent<QuestCompleteEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestComplete(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestCompleteEvent.class);
    }

    @Override
    public void invoke(QuestCompleteEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
