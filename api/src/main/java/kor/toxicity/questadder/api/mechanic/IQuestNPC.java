package kor.toxicity.questadder.api.mechanic;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IQuestNPC {
    @Nullable Integer getIndex(Player player);
    boolean setIndex(Player player, int index);
    @NotNull String getKey();
}
