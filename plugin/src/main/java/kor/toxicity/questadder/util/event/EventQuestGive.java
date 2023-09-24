package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestGiveEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.jetbrains.annotations.NotNull;

public class EventQuestGive extends AbstractEvent<QuestGiveEvent> {
    @DataField(aliases = "n")
    public String name;

    public EventQuestGive(QuestAdder adder, AbstractAction action) {
        super(adder, action, QuestGiveEvent.class);
    }

    @Override
    public void invoke(@NotNull QuestGiveEvent event) {
        var quest = event.getQuest();
        var key = quest.getKey();
        if (name != null && !name.equals(key)) return;
        apply(event.getPlayer(),event);
    }
}
