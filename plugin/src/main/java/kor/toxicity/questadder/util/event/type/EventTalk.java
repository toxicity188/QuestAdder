package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.TalkStartEvent;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;

public class EventTalk extends AbstractEvent<TalkStartEvent> {
    @DataField(aliases = "k")
    public String key;
    @DataField(aliases = "d")
    public String dialog;

    public EventTalk(QuestAdder adder, AbstractAction action) {
        super(adder, action, TalkStartEvent.class);
    }

    @Override
    protected void invoke(TalkStartEvent event) {
        if (key != null && !key.equals(event.getNpc().getQuestNPC().getKey())) return;
        if (dialog != null && !dialog.equals(event.getDialog().getKey())) return;
        apply(event.getPlayer());
    }
}
