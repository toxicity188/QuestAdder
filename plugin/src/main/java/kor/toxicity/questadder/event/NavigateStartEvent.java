package kor.toxicity.questadder.event;

import kor.toxicity.questadder.util.NamedLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NavigateStartEvent extends QuestAdderPlayerEvent implements LocationEvent {
    private final NamedLocation namedLocation;
    public NavigateStartEvent(@NotNull Player who, @NotNull NamedLocation location) {
        super(who);
        this.namedLocation = location;
    }

    public NamedLocation getNamedLocation() {
        return namedLocation;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}