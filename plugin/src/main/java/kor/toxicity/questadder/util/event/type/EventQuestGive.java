package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestGiveEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventQuestGive extends AbstractEvent<QuestGiveEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestGive(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestGiveEvent.class);
    }

    @Override
    protected void invoke(QuestGiveEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
