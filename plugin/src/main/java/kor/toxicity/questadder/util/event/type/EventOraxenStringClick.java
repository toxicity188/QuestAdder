package kor.toxicity.questadder.util.event.type;

import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockInteractEvent;
import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.Bukkit;

import java.util.function.Predicate;

public class EventOraxenStringClick extends AbstractEvent<OraxenStringBlockInteractEvent> {

    @DataField(aliases = {"loc","l"})
    public String location;
    @DataField(aliases = "w")
    public String world;
    @DataField(aliases = "i")
    public String id;

    private Predicate<OraxenStringBlockInteractEvent> predicate = e -> true;

    public EventOraxenStringClick(QuestAdder adder, AbstractAction action) {
        super(adder, action, OraxenStringBlockInteractEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (location != null) {
            var split = location.split(",");
            if (split.length == 3) {
                try {
                    var x = Integer.parseInt(split[0]);
                    var y = Integer.parseInt(split[1]);
                    var z = Integer.parseInt(split[2]);
                    predicate = predicate.and(e -> {
                        var loc = e.getBlock().getLocation();
                        return loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z;
                    });
                } catch (Exception e) {
                    throw new RuntimeException("a location format must be \"x,y,z\".");
                }
            } else throw new RuntimeException("a location format must be \"x,y,z\".");
        }
        if (world != null) {
            var w = Bukkit.getWorld(world);
            if (w == null) throw new RuntimeException("the world named \"" + world + "\" doesn't exist.");
            predicate = predicate.and(e -> e.getPlayer().getWorld().equals(w));
        }
        if (id != null) {
            predicate = predicate.and(e -> e.getMechanic().getItemID().equals(id));
        }
        world = null;
        location = null;
    }

    @Override
    protected void invoke(OraxenStringBlockInteractEvent event) {
        if (predicate.test(event)) apply(event.getPlayer());
    }
}
