package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.TalkStartEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;

public class EventTalk extends AbstractEvent<TalkStartEvent> {
    @DataField(aliases = "k")
    public String key;
    @DataField(aliases = "d")
    public String dialog;

    public EventTalk(QuestAdder adder, AbstractAction action) {
        super(adder, action, TalkStartEvent.class);
    }

    @Override
    public void invoke(TalkStartEvent event) {
        if (key != null && !key.equals(event.getNpc().toQuestNPC().getKey())) return;
        if (dialog != null && !dialog.equals(event.getDialog().getKey())) return;
        apply(event.getPlayer(),event);
    }
}
