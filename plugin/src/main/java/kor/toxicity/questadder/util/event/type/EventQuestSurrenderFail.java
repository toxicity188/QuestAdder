package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.api.event.QuestSurrenderFailEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventQuestSurrenderFail extends AbstractEvent<QuestSurrenderFailEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestSurrenderFail(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestSurrenderFailEvent.class);
    }

    @Override
    protected void invoke(QuestSurrenderFailEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
