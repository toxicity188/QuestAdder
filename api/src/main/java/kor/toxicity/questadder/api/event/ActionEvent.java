package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IAction;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface ActionEvent {
    @NotNull IAction getAction();
}
