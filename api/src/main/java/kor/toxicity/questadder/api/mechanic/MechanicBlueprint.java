package kor.toxicity.questadder.api.mechanic;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public interface MechanicBlueprint {
    @NotNull ConfigurationSection getConfig();
}
