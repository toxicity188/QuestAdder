package kor.toxicity.questadder.api.item;

import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

@FunctionalInterface
public interface ItemSupplier extends Supplier<ItemStack> {
}
