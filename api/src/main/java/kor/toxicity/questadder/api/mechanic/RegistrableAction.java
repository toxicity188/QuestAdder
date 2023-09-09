package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.QuestAdder;
import org.jetbrains.annotations.NotNull;

public abstract class RegistrableAction extends CancellableAction {

    public RegistrableAction(@NotNull QuestAdder adder) {
        super(adder);
    }
    public abstract void unregister();
}
