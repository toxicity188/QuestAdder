package kor.toxicity.questadder.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GuiHolder implements InventoryHolder {
    private final Inventory inventory;
    private final GuiData data;
    private final GuiExecutor executor;
    public GuiHolder(@NotNull IGui gui, int size, @NotNull Component name, @NotNull Player player, @NotNull GuiExecutor executor, @NotNull Map<Integer, ItemStack> map) {
        inventory = Bukkit.createInventory(this, size, name);
        map.forEach(inventory::setItem);
        data = new GuiData(gui, inventory, executor, player);
        this.executor = executor;
        executor.initialize(data);
    }

    public @NotNull GuiExecutor getExecutor() {
        return executor;
    }

    public @NotNull GuiData getData() {
        return data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
