package kor.toxicity.questadder.api.mechanic;

import org.jetbrains.annotations.NotNull;

public interface IDialogState {
    void addEndTask(@NotNull Runnable runnable);
}
