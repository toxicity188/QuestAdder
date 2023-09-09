package kor.toxicity.questadder.util.event;

import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.function.Predicate;

public class EventCustomBlockPlace extends AbstractEvent<CustomBlockPlaceEvent> {
    @DataField(aliases = "t")
    public Material type;
    @DataField(aliases = {"loc","l"})
    public String location;
    @DataField(aliases = "w")
    public String world;
    @DataField(aliases = "i")
    public String id;

    private Predicate<CustomBlockPlaceEvent> predicate = e -> true;

    public EventCustomBlockPlace(QuestAdder adder, AbstractAction action) {
        super(adder, action, CustomBlockPlaceEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (type != null) predicate = predicate.and(e -> e.getBlock().getType() == type);
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
        if (id != null) predicate = predicate.and(e -> e.getNamespacedID().equals(id));

        location = null;
        world = null;
    }
    @Override
    public void invoke(CustomBlockPlaceEvent event) {
        if (predicate.test(event)) apply(event.getPlayer());
    }
}
