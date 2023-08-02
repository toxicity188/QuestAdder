package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class CancellableAction extends AbstractAction {

    public CancellableAction(@NotNull QuestAdder adder) {
        super(adder);
    }
    public abstract void cancel(@NotNull Player player);
}
