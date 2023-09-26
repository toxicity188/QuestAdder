package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.manager.LocationManager;
import kor.toxicity.questadder.manager.NavigationManager;
import kor.toxicity.questadder.util.NamedLocation;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActStartNavigate extends AbstractAction {
    @DataField(aliases = {"l","loc"}, throwIfNull = true)
    public String location;

    private NamedLocation namedLocation;
    public ActStartNavigate(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        namedLocation = LocationManager.INSTANCE.getLocation(location);
        if (namedLocation == null) throw new RuntimeException("the location named \"" + location + "\" doesn't exist.");
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        NavigationManager.INSTANCE.startNavigate(player, namedLocation);
        return ActionResult.SUCCESS;
    }
}
