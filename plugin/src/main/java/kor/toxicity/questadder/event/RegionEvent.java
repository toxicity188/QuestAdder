package kor.toxicity.questadder.event;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface RegionEvent {
    ProtectedRegion getRegion();
}
