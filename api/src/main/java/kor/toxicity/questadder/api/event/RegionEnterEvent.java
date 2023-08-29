package kor.toxicity.questadder.api.event;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RegionEnterEvent extends QuestAdderPlayerEvent implements RegionEvent {

    private final ProtectedRegion region;

    public RegionEnterEvent(@NotNull Player who, @NotNull ProtectedRegion region) {
        super(who);
        this.region = region;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
