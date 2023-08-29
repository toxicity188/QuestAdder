package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ItemDatabase {

    Collection<String> getKeys();
    @Nullable
    ItemStack getItem(String name);
    @NotNull
    Collection<ItemPair> getItems();
    void reload();
    @NotNull String requiredPlugin();
}
