package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestRemoveEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventQuestRemove extends AbstractEvent<QuestRemoveEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestRemove(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestRemoveEvent.class);
    }

    @Override
    protected void invoke(QuestRemoveEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
