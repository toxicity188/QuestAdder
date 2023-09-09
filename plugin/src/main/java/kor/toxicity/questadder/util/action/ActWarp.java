package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.LocationManager;
import kor.toxicity.questadder.util.NamedLocation;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActWarp extends AbstractAction {
    @DataField(aliases = {"loc", "l"}, throwIfNull = true)
    public String location;
    @DataField
    public boolean packet = false;

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
        if (packet) QuestAdderBukkit.Companion.getNms().changePosition(player,loc.getLocation());
        else player.teleport(loc.getLocation());
    }
}
