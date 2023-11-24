package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.mechanic.IDialog;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public interface DialogEvent {
    @NotNull IDialog getDialog();
}
