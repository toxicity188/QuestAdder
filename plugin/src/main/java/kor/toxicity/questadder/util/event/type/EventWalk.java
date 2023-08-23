package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class EventWalk extends AbstractEvent<PlayerMoveEvent> {

    @DataField(aliases = "t")
    public Material type;
    @DataField(aliases = "w")
    public String world;
    @DataField(aliases = {"l","loc"})
    public String location;
    @DataField(aliases = "r")
    public double range = 1;

    private Predicate<Player> predicate = p -> true;

    public EventWalk(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerMoveEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        range = Math.max(range,0.5);

        if (world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) throw new RuntimeException("the world \"" + world + "\" doesn't exist.");
            var t = predicate;
            predicate = p -> t.test(p) && p.getWorld().equals(w);
        }
        if (location != null) {
            var split = location.split(",");
            if (split.length == 3) {
                try {
                    var vec = new Vector(
                            Double.parseDouble(split[0]),
                            Double.parseDouble(split[1]),
                            Double.parseDouble(split[2])
                    );
                    var t = predicate;
                    predicate = p -> t.test(p) && p.getLocation().toVector().distance(vec) <= range;
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("location format must be \"x,y,z\"");
                }
            } else throw new RuntimeException("location format must be \"x,y,z\"");
        }
        if (type != null) {
            var t = predicate;
            predicate = p -> t.test(p) && p.getLocation().add(0,-1,0).getBlock().getType() == type;
        }
        type = null;
        location = null;
        world = null;
    }

    @Override
    protected void invoke(PlayerMoveEvent event) {
        var player = event.getPlayer();
        if (predicate.test(player)) apply(player);
    }
}
