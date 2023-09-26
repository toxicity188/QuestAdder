package kor.toxicity.questadder.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IGui {
    @NotNull GuiHolder open(@NotNull Player player, @NotNull GuiExecutor executor);
    @NotNull
    Component getGuiName();
    @NotNull
    Map<Integer, ItemStack> getInnerItems();
}
