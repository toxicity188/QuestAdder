package kor.toxicity.questadder.api.block;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface IQuestBlock {
    void place(@NotNull Location location);
    @NotNull BlockBluePrint getBluePrint();
    @NotNull String getKey();
}
