package kor.toxicity.questadder.api.npc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface WrappedNPC {
    @NotNull UUID getUUID();
    @NotNull Object getHandle();
    @Nullable Location getLocation();
    @Nullable World getWorld();
    float getEyeHeight();
    @Nullable Entity getEntity();
}
