package kor.toxicity.questadder.api.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface IGuiHolder extends InventoryHolder {
    @NotNull GuiData getData();

    @Override
    @NotNull Inventory getInventory();
}
