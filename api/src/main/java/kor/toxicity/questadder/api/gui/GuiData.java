package kor.toxicity.questadder.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuiData {
    private final IGui gui;
    private final Inventory inventory;
    private GuiExecutor executor;
    private final Player player;

    public GuiData(@NotNull IGui gui, @NotNull Inventory inventory, @NotNull GuiExecutor executor, @NotNull Player player) {
        this.gui = gui;
        this.inventory = inventory;
        this.executor = executor;
        this.player = player;
    }

    public @NotNull GuiExecutor getExecutor() {
        return executor;
    }

    public @NotNull IGui getGui() {
        return gui;
    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public void setExecutor(@NotNull GuiExecutor executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public void reopen() {
        gui.open(player,executor);
    }
}
