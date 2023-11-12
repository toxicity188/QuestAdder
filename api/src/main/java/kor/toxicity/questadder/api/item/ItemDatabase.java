package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ItemDatabase {

    @NotNull Collection<@NotNull String> getKeys();
    @Nullable
    ItemStack getItem(@NotNull String name);
    @NotNull
    Collection<@NotNull ItemPair> getItems();
    void reload();
    @NotNull String requiredPlugin();
}
