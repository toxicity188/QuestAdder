package kor.toxicity.questadder.api.exp;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ExpHandler {
    void accept(@NotNull Player player, double exp);
    @NotNull String requiredPlugin();
}
