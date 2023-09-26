package kor.toxicity.questadder.api.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface GuiExecutor {
    void initialize(@NotNull GuiData data);
    void click(@NotNull GuiData data, @NotNull ItemStack clickedItem, int clickedSlot, boolean isPlayerInventory, @NotNull MouseButton button);
    void end(@NotNull GuiData data);
}
