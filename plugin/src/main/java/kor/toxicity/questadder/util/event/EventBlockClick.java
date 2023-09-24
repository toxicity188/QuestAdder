package kor.toxicity.questadder.util.event;

import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.AbstractEvent;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class EventBlockClick extends AbstractEvent<PlayerInteractEvent> {
    @DataField(aliases = "t")
    public Material type;
    @DataField(aliases = {"loc","l"})
    public String location;
    @DataField(aliases = "w")
    public String world;
    @DataField(aliases = "h")
    public String held;

    private BiPredicate<PlayerInteractEvent, Block> predicate = (e,b) -> true;

    public EventBlockClick(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerInteractEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (type != null) predicate = predicate.and((e,b) -> b.getType() == type);
        if (location != null) {
            var split = location.split(",");
            if (split.length == 3) {
                try {
                    var x = Integer.parseInt(split[0]);
                    var y = Integer.parseInt(split[1]);
                    var z = Integer.parseInt(split[2]);
                    predicate = predicate.and((e,b) -> {
                        var loc = b.getLocation();
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
            predicate = predicate.and((e,b) -> e.getPlayer().getWorld().equals(w));
        }
        if (held != null) {
            var item = ItemManager.INSTANCE.getItem(held);
            if (item == null) throw new RuntimeException("the item named \"" + held + "\" doesn't exist.");
            predicate = predicate.and((e,b) -> e.getPlayer().getInventory().getItemInMainHand().isSimilar(item));
        }

        location = null;
        world = null;
        held = null;
    }
    @Override
    public void invoke(@NotNull PlayerInteractEvent event) {
        var block = event.getClickedBlock();
        if (block != null && predicate.test(event,block)) apply(event.getPlayer());
    }
}
