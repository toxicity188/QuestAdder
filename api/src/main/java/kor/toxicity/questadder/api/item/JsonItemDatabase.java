package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JsonItemDatabase extends ItemDatabase {
    @Nullable
    ItemSupplier getItemSupplier(@NotNull String name, @NotNull String jsonObject);

    default @Nullable ItemStack getItemStack(@NotNull String name, @NotNull String jsonObject) {
        ItemSupplier supplier = getItemSupplier(name, jsonObject);
        return supplier != null ? supplier.get() : null;
    }
}
