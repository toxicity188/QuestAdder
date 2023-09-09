package kor.toxicity.questadder.api.mechanic;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IQuestNPC {
    @Nullable Integer getIndex(@NotNull Player player);
    void getIndexAsync(@NotNull Player player, @NotNull Consumer<Integer> consumer);
    boolean setIndex(@NotNull Player player, int index);
    void setIndexAsync(@NotNull OfflinePlayer player, int index);
    @NotNull String getKey();
}
