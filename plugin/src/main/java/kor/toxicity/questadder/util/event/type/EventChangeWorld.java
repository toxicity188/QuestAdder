package kor.toxicity.questadder.util.event.type;

import kor.toxicity.questadder.QuestAdder;
import kor.toxicity.questadder.util.action.AbstractAction;
import kor.toxicity.questadder.util.event.AbstractEvent;
import kor.toxicity.questadder.util.reflect.DataField;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.function.Predicate;

public class EventChangeWorld extends AbstractEvent<PlayerChangedWorldEvent> {

    @DataField(aliases = "f")
    public String from;
    @DataField(aliases = "t")
    public String to;

    private Predicate<PlayerChangedWorldEvent> predicate = e -> true;

    public EventChangeWorld(QuestAdder adder, AbstractAction action) {
        super(adder, action, PlayerChangedWorldEvent.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (from != null) {
            var world = Bukkit.getWorld(from);
            if (world == null) throw new RuntimeException("the world named \"" + from + "\" doesn't exist.");
            predicate = predicate.and(e -> e.getFrom().equals(world));
        }
        if (to != null) {
            var world = Bukkit.getWorld(to);
            if (world == null) throw new RuntimeException("the world named \"" + to + "\" doesn't exist.");
            predicate = predicate.and(e -> e.getPlayer().getWorld().equals(world));
        }
        from = null;
        to = null;
    }

    @Override
    protected void invoke(PlayerChangedWorldEvent event) {
        if (predicate.test(event)) apply(event.getPlayer());
    }
}
