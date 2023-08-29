package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestCompleteEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventQuestComplete extends AbstractEvent<QuestCompleteEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestComplete(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestCompleteEvent.class);
    }

    @Override
    protected void invoke(QuestCompleteEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
