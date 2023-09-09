package kor.toxicity.questadder.api.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface INamedLocation {
    @NotNull String getKey();
    @NotNull Location getLocation();
}
