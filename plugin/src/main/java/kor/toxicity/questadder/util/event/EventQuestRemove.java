package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestRemoveEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventQuestRemove extends AbstractEvent<QuestRemoveEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestRemove(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestRemoveEvent.class);
    }

    @Override
    public void invoke(QuestRemoveEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
