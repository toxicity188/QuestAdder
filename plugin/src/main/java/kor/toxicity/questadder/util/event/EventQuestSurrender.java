package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestSurrenderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventQuestSurrender extends AbstractEvent<QuestSurrenderEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestSurrender(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestSurrenderEvent.class);
    }

    @Override
    public void invoke(QuestSurrenderEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
