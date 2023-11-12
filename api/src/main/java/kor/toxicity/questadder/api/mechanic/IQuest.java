package kor.toxicity.questadder.api.mechanic;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.SortedSet;

public interface IQuest {
    void give(@NotNull Player player);
    void remove(@NotNull Player player);
    void complete(@NotNull Player player);

    boolean has(@NotNull Player player);
    boolean isCompleted(@NotNull Player player);
    boolean isCleared(@NotNull Player player);
    boolean isReady(@NotNull Player player);

    @NotNull ItemStack getIcon(@NotNull Player player, @NotNull List<Component> components);
    @NotNull String getName();
    @NotNull String getKey();
    @NotNull SortedSet<@NotNull String> getTypes();
}
