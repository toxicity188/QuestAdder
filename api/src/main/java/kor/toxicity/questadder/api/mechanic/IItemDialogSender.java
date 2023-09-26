package kor.toxicity.questadder.api.mechanic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IItemDialogSender extends DialogSender {
    void give(@NotNull Player player);
    @Nullable IDialogState start(@NotNull Player player);
}
