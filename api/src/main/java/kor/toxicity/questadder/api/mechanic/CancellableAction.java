package kor.toxicity.questadder.api.mechanic;

import kor.toxicity.questadder.api.QuestAdder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class CancellableAction extends AbstractAction {

    public CancellableAction(@NotNull QuestAdder adder) {
        super(adder);
    }
    public abstract void cancel(@NotNull Player player);
}
