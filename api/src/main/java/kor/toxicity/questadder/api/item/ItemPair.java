package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemPair(@NotNull String key, @NotNull ItemStack stack) {
}
