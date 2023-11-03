package kor.toxicity.questadder.api.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SoundData(@NotNull String name, float volume, float pitch) {
    public static @Nullable SoundData fromString(@NotNull String string) {
        var split = string.split(" ");
        try {
            return new SoundData(split[0], split.length > 1 ? Float.parseFloat(split[1]) : 1F, split.length > 2 ? Float.parseFloat(split[2]) : 1F);
        } catch (Exception e) {
            return null;
        }
    }

    public void play(@NotNull Player player) {
        player.playSound(player.getLocation(), name, volume, pitch);
    }

    @Override
    public String toString() {
        return name + " " + volume + " " + pitch;
    }
}
