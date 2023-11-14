package kor.toxicity.questadder.api.util;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface IPlayerData {
    @NotNull ConfigurationSection serialize();
}
