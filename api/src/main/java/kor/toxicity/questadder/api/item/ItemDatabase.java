package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public interface ItemDatabase {

    @Unmodifiable
    @NotNull
    Collection<@NotNull String> getKeys();
    @Nullable
    ItemStack getItem(@NotNull String name);

    @Unmodifiable
    @NotNull
    Collection<@NotNull ItemPair> getItems();
    void reload();
    @NotNull String requiredPlugin();
}
