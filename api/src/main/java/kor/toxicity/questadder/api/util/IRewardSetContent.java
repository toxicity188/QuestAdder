package kor.toxicity.questadder.api.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IRewardSetContent {
    @NotNull ItemStack getItem();
    double getChance();
}
