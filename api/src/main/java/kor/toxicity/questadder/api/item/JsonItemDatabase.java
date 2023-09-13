package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JsonItemDatabase extends ItemDatabase {
    @Nullable
    ItemStack getItem(@NotNull String name, @NotNull String jsonObject);
}
