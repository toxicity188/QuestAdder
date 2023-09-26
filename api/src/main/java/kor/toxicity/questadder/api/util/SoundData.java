package kor.toxicity.questadder.api.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SoundData(@NotNull String name, float volume, float pitch) {
    public static @Nullable SoundData fromString(@NotNull String string) {
        var split = string.split(" ");
        if (split.length < 3) return null;
        try {
            return new SoundData(split[0], Float.parseFloat(split[1]), Float.parseFloat(split[2]));
        } catch (Exception e) {
            return null;
        }
    }

    public void play(@NotNull Player player) {
        player.playSound(player.getLocation(), name, volume, pitch);
    }
}
