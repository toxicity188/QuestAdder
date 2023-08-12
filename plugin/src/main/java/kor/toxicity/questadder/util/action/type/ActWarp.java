package kor.toxicity.questadder.util.action.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.LocationManager;
import kor.toxicity.questadder.util.NamedLocation;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActWarp extends AbstractAction {
    @DataField(aliases = {"loc", "l"}, throwIfNull = true)
    public String location;

    public ActWarp(@NotNull QuestAdder adder) {
        super(adder);
    }
    private NamedLocation loc;
    @Override
    public void initialize() {
        super.initialize();
        loc = LocationManager.INSTANCE.getLocation(location);
        if (loc == null) throw new RuntimeException("the location named \"" + location + "\" doesn't exist.");
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        player.teleport(loc.getLocation());
    }
}
