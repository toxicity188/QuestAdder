package kor.toxicity.questadder.api.mechanic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDialog {
    /**
     * Get a key of that Dialog
     * @return A yaml key of Dialog
     */
    @NotNull String getKey();

    /**
     * Start Dialog as that sender.
     * @param player target player
     * @param sender talker
     * @return the state of dialog or null if start is fail for some reason.
     */
    @Nullable
    IDialogState start(@NotNull Player player, @NotNull DialogSender sender);
}
