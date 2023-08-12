package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestGiveEvent;
import kor.toxicity.questadder.event.QuestSurrenderEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventQuestSurrender extends AbstractEvent<QuestSurrenderEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestSurrender(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestSurrenderEvent.class);
    }

    @Override
    protected void invoke(QuestSurrenderEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
