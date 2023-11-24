package kor.toxicity.questadder.api.event;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.HandlerList;

public interface RegionEvent {
    ProtectedRegion getRegion();
}
