package kor.toxicity.questadder.api.shop;

import kor.toxicity.questadder.api.mechanic.DialogSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IShop {
    void open(@NotNull Player player, @NotNull DialogSender sender);
    @NotNull String getKey();
}
