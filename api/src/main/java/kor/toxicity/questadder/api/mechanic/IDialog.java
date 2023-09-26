package kor.toxicity.questadder.api.mechanic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDialog {
    @NotNull String getKey();
    @Nullable
    IDialogState start(@NotNull Player player, @NotNull DialogSender sender);
}
