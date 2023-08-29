package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.util.INamedLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NavigateStartEvent extends QuestAdderPlayerEvent implements LocationEvent {
    private final INamedLocation namedLocation;
    public NavigateStartEvent(@NotNull Player who, @NotNull INamedLocation location) {
        super(who);
        this.namedLocation = location;
    }

    public INamedLocation getNamedLocation() {
        return namedLocation;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
