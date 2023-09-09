package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestSurrenderFailEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventQuestSurrenderFail extends AbstractEvent<QuestSurrenderFailEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestSurrenderFail(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestSurrenderFailEvent.class);
    }

    @Override
    public void invoke(QuestSurrenderFailEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
